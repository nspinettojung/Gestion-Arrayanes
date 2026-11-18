package com.Gestion.Arrayanes.Backup;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Crea, lista y marca copias H2 con {@code BACKUP TO}. */
@Slf4j
@Service
public class BackupService {

    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");
    private static final Pattern FILE_NAME =
            Pattern.compile("backup-([a-z]+)-(\\d{8}-\\d{6})(?:-\\d+)?\\.zip");
    private static final Comparator<Path> NEWEST_FIRST =
            Comparator.comparingLong((Path path) -> modifiedAt(path).toEpochMilli())
                    .reversed()
                    .thenComparing(path -> path.getFileName().toString(), Comparator.reverseOrder());

    private final Path directory;
    private final Path databaseFile;
    private final JdbcTemplate jdbc;
    private final Clock clock;
    private final int maxCopies;
    private final Pattern safetyCopyName;

    public BackupService(
            @Value("${arrayanes.backup.directory:./datos/backups}") String directory,
            @Value("${spring.datasource.url:}") String datasourceUrl,
            @Value("${arrayanes.backup.max-copies:10}") int maxCopies,
            JdbcTemplate jdbc,
            Clock clock) {
        this.directory = Path.of(directory).toAbsolutePath().normalize();
        this.databaseFile = BackupFiles.databaseFileFromJdbcUrl(datasourceUrl)
                .map(path -> path.toAbsolutePath().normalize())
                .orElse(null);
        this.maxCopies = Math.max(1, maxCopies);
        this.jdbc = jdbc;
        this.clock = clock;
        this.safetyCopyName = this.databaseFile == null
                ? null
                : Pattern.compile(Pattern.quote(this.databaseFile.getFileName() + ".bak-")
                        + "\\d{8}-\\d{6}");
    }

    public BackupInfo createManual() {
        return create("manual");
    }

    public BackupInfo createScheduled() {
        return create("diario");
    }

    public BackupInfo createOnShutdown() {
        return create("cierre");
    }

    private BackupInfo create(String kind) {
        if (databaseFile == null) {
            throw new BadRequestException(
                    "La base de datos configurada no es un archivo H2; no se pueden crear copias.");
        }
        try {
            Files.createDirectories(directory);
            String name = nextName(kind);
            Path target = directory.resolve(name);
            String sql = "BACKUP TO '" + BackupFiles.escapeSqlLiteral(target.toString()) + "'";
            jdbc.execute(sql);
            log.info("Copia de seguridad creada: {}", name);
            pruneOldFiles();
            return toInfo(target, false);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo crear la copia de seguridad: " + e.getMessage(), e);
        }
    }

    private String nextName(String kind) {
        String stamp = STAMP.withZone(ZoneId.systemDefault()).format(clock.instant());
        String base = "backup-" + kind + "-" + stamp;
        String name = base + ".zip";
        int suffix = 2;
        while (Files.exists(directory.resolve(name))) {
            name = base + "-" + suffix++ + ".zip";
        }
        return name;
    }

    public BackupListResponse list() {
        String pending = pendingRestore().orElse(null);

        if (!Files.isDirectory(directory)) {
            return new BackupListResponse(List.of(), pending);
        }

        try (Stream<Path> files = Files.list(directory)) {
            List<BackupInfo> backups = files
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".zip"))
                    .sorted(NEWEST_FIRST)
                    .map(path -> toInfo(path, path.getFileName().toString().equals(pending)))
                    .toList();
            return new BackupListResponse(backups, pending);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudieron listar las copias de seguridad.", e);
        }
    }

    /**
     * Mantiene como máximo `maxCopies` zips y copias de seguridad previas a una
     * restauración. Nunca borra la copia marcada para restaurar. La limpieza es
     * best-effort: un fallo al borrar no debe fallar la copia recién creada.
     */
    private void pruneOldFiles() {
        try {
            String pending = pendingRestore().orElse(null);
            try (Stream<Path> files = Files.list(directory)) {
                files.filter(Files::isRegularFile)
                        .filter(path -> path.getFileName().toString().endsWith(".zip"))
                        .sorted(NEWEST_FIRST)
                        .skip(maxCopies)
                        .filter(path -> !path.getFileName().toString().equals(pending))
                        .forEach(this::deleteQuietly);
            }
            pruneSafetyCopies();
        } catch (IOException e) {
            log.warn("No se pudo aplicar la retención de copias: {}", e.getMessage());
        }
    }

    private void pruneSafetyCopies() throws IOException {
        if (safetyCopyName == null || databaseFile.getParent() == null) {
            return;
        }
        try (Stream<Path> files = Files.list(databaseFile.getParent())) {
            files.filter(Files::isRegularFile)
                    .filter(path -> safetyCopyName.matcher(path.getFileName().toString()).matches())
                    .sorted(NEWEST_FIRST)
                    .skip(maxCopies)
                    .forEach(this::deleteQuietly);
        }
    }

    private void deleteQuietly(Path file) {
        try {
            Files.delete(file);
            log.info("Copia antigua eliminada por retención: {}", file.getFileName());
        } catch (IOException e) {
            log.warn("No se pudo eliminar la copia antigua {}: {}", file.getFileName(), e.getMessage());
        }
    }

    /** Marca una copia para que se restaure antes de abrir la base en el próximo arranque. */
    public BackupInfo requestRestore(String name) {
        Path backup = BackupFiles.resolve(directory, name);
        if (!Files.isRegularFile(backup)) {
            throw new ResourceNotFoundException("No existe la copia de seguridad indicada.");
        }

        try {
            Files.writeString(
                    directory.resolve(BackupFiles.MARKER),
                    name,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new IllegalStateException("No se pudo registrar la copia para restaurar.", e);
        }

        log.info("Copia marcada para restaurar en el próximo arranque: {}", name);
        return toInfo(backup, true);
    }

    public Optional<String> pendingRestore() {
        Path marker = directory.resolve(BackupFiles.MARKER);
        if (!Files.isRegularFile(marker)) {
            return Optional.empty();
        }
        try {
            String name = Files.readString(marker, StandardCharsets.UTF_8).trim();
            return BackupFiles.isValidName(name) ? Optional.of(name) : Optional.empty();
        } catch (IOException e) {
            log.warn("No se pudo leer el marcador de restauración: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private BackupInfo toInfo(Path file, boolean pending) {
        String name = file.getFileName().toString();
        Matcher matcher = FILE_NAME.matcher(name);
        boolean known = matcher.matches();
        String kind = known ? matcher.group(1) : "otro";
        Instant createdAt = known ? parseStamp(matcher.group(2)).orElseGet(() -> modifiedAt(file)) : modifiedAt(file);

        long sizeBytes;
        try {
            sizeBytes = Files.size(file);
        } catch (IOException e) {
            sizeBytes = 0;
        }
        return new BackupInfo(name, kind, createdAt, sizeBytes, pending);
    }

    private Optional<Instant> parseStamp(String stamp) {
        try {
            return Optional.of(
                    LocalDateTime.parse(stamp, STAMP).atZone(ZoneId.systemDefault()).toInstant());
        } catch (DateTimeParseException e) {
            return Optional.empty();
        }
    }

    private static Instant modifiedAt(Path file) {
        try {
            return Files.getLastModifiedTime(file).toInstant();
        } catch (IOException e) {
            return Instant.EPOCH;
        }
    }
}
