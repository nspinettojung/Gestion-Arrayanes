package com.Gestion.Arrayanes.Backup;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;

import java.nio.file.Path;
import java.util.Optional;
import java.util.regex.Pattern;

/** Utilidades compartidas entre la creación y la restauración de copias. */
public final class BackupFiles {

    public static final String MARKER = ".restore-pending";

    /** Los nombres de copia son simples: nunca pueden contener separadores ni `..` suelto. */
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._-]{0,119}\\.zip");

    private BackupFiles() {
    }

    public static boolean isValidName(String name) {
        return name != null && SAFE_NAME.matcher(name).matches();
    }

    public static void validateName(String name) {
        if (!isValidName(name)) {
            throw new BadRequestException("El nombre de la copia no es válido.");
        }
    }

    /** Resuelve un nombre dentro del directorio de copias y valida que no pueda salir de él. */
    public static Path resolve(Path directory, String name) {
        validateName(name);
        Path base = directory.toAbsolutePath().normalize();
        Path candidate = base.resolve(name).normalize();
        if (!candidate.getParent().equals(base)) {
            throw new BadRequestException("El nombre de la copia no es válido.");
        }
        return candidate;
    }

    /** El zip de `BACKUP TO` guarda el archivo con el mismo nombre que la base. */
    public static String backupEntryName(Path databaseFile) {
        return databaseFile.getFileName().toString();
    }

    /** Extrae la ruta del archivo `.mv.db` de una URL H2 de archivo. */
    public static Optional<Path> databaseFileFromJdbcUrl(String url) {
        String prefix = "jdbc:h2:file:";
        if (url == null || !url.startsWith(prefix)) {
            return Optional.empty();
        }
        String spec = url.substring(prefix.length());
        int separator = spec.indexOf(';');
        if (separator >= 0) {
            spec = spec.substring(0, separator);
        }
        if (spec.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(Path.of(spec + ".mv.db"));
    }

    public static String escapeSqlLiteral(String value) {
        return value.replace("'", "''");
    }
}
