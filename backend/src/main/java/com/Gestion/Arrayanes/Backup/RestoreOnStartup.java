package com.Gestion.Arrayanes.Backup;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.Properties;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Aplica la copia marcada desde la UI antes de que Spring abra la base de datos.
 * Reemplazar el `.mv.db` con la app corriendo dejaría la base en un estado
 * inconsistente, por eso la restauración siempre ocurre en este punto del arranque.
 */
@Slf4j
public final class RestoreOnStartup {

    private static final DateTimeFormatter SAFETY_STAMP = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss");

    private RestoreOnStartup() {
    }

    public record RestoreResult(boolean applied, String backupName, String error) {
    }

    /** Punto de entrada desde {@code main}, antes de crear el contexto Spring. */
    public static void restorePendingBackup(String[] args) {
        Properties properties = loadProperties(args);
        String directory = properties.getProperty("arrayanes.backup.directory", "./datos/backups");
        String datasourceUrl = properties.getProperty("spring.datasource.url", "");

        Optional<Path> database = BackupFiles.databaseFileFromJdbcUrl(datasourceUrl);
        if (database.isEmpty()) {
            log.warn("La base no es un archivo H2; se omite la restauración pendiente.");
            return;
        }

        RestoreResult result = restore(Path.of(directory), database.get());
        if (result.error() != null) {
            log.error("No se pudo restaurar la copia {}: {}", result.backupName(), result.error());
        } else if (result.applied()) {
            log.info("Copia {} restaurada antes del arranque.", result.backupName());
        }
    }

    /** Lógica pura, separada para poder verificarla sin iniciar la aplicación. */
    public static RestoreResult restore(Path directory, Path databaseFile) {
        Path base = directory.toAbsolutePath().normalize();
        Path marker = base.resolve(BackupFiles.MARKER);

        if (!Files.isRegularFile(marker)) {
            return new RestoreResult(false, null, null);
        }

        String name;
        try {
            name = Files.readString(marker, StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return new RestoreResult(false, null, "No se pudo leer la copia pendiente: " + e.getMessage());
        }

        if (!BackupFiles.isValidName(name)) {
            return new RestoreResult(false, name, "El nombre de la copia pendiente no es válido.");
        }

        Path backup = base.resolve(name);
        if (!Files.isRegularFile(backup)) {
            return new RestoreResult(false, name, "No se encontró la copia pendiente " + name);
        }

        try {
            replaceDatabase(backup, databaseFile, name);
            // Solo se borra el marcador después de reemplazar el archivo con éxito.
            Files.deleteIfExists(marker);
            return new RestoreResult(true, name, null);
        } catch (Exception e) {
            return new RestoreResult(false, name, e.getMessage());
        }
    }

    private static void replaceDatabase(Path backup, Path databaseFile, String backupName)
            throws IOException {
        Path target = databaseFile.toAbsolutePath().normalize();
        Path parent = target.getParent();
        Files.createDirectories(parent);
        Path extracted = Files.createTempFile(parent, "restore-", ".mv.db");

        try {
            try (ZipFile zip = new ZipFile(backup.toFile())) {
                ZipEntry entry = zip.getEntry(BackupFiles.backupEntryName(databaseFile));
                if (entry == null) {
                    // Por si la base cambió de nombre: aceptamos cualquier .mv.db del zip.
                    entry = zip.stream()
                            .filter(candidate -> !candidate.isDirectory()
                                    && candidate.getName().endsWith(".mv.db"))
                            .findFirst()
                            .orElse(null);
                }
                if (entry == null) {
                    throw new IOException("La copia " + backupName + " no contiene un archivo .mv.db");
                }
                try (InputStream in = zip.getInputStream(entry)) {
                    Files.copy(in, extracted, StandardCopyOption.REPLACE_EXISTING);
                }
            }

            if (Files.size(extracted) == 0) {
                throw new IOException("La copia está vacía.");
            }

            if (Files.exists(target)) {
                String stamp = LocalDateTime.now().format(SAFETY_STAMP);
                Path safetyCopy = parent.resolve(target.getFileName() + ".bak-" + stamp);
                Files.copy(target, safetyCopy, StandardCopyOption.REPLACE_EXISTING);
            }

            Files.move(extracted, target, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(extracted);
        }
    }

    /**
     * Lee `application.properties` sin depender de Spring y aplica como override
     * las propiedades del sistema y los argumentos `--clave=valor`.
     */
    private static Properties loadProperties(String[] args) {
        Properties properties = new Properties();
        try (InputStream in = RestoreOnStartup.class.getClassLoader()
                .getResourceAsStream("application.properties")) {
            if (in != null) {
                properties.load(in);
            }
        } catch (IOException e) {
            log.warn("No se pudo leer application.properties: {}", e.getMessage());
        }

        for (String key : System.getProperties().stringPropertyNames()) {
            properties.setProperty(key, System.getProperty(key));
        }
        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("--") && arg.contains("=")) {
                    int separator = arg.indexOf('=');
                    properties.setProperty(arg.substring(2, separator), arg.substring(separator + 1));
                }
            }
        }
        return properties;
    }
}
