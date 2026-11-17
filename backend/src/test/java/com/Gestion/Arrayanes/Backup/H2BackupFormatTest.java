package com.Gestion.Arrayanes.Backup;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Documenta por qué la restauración no usa `RUNSCRIPT FROM`: `BACKUP TO`
 * genera un zip con el archivo binario `.mv.db`, no un script SQL.
 */
class H2BackupFormatTest {

    @Test
    void elBackupContieneElArchivoDeBaseYCopiarloRestauraLosDatos(@TempDir Path dir) throws Exception {
        String base = dir.resolve("mibasedatos").toString().replace('\\', '/');
        String url = "jdbc:h2:file:" + base;
        Path backup = dir.resolve("backup.zip");

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE PRUEBA (ID INT PRIMARY KEY, NOMBRE VARCHAR(50))");
            statement.execute("INSERT INTO PRUEBA VALUES (1, 'uno')");
            statement.execute("BACKUP TO '" + backup.toString().replace('\\', '/') + "'");
        }

        Path restaurada = dir.resolve("restaurada.mv.db");
        try (ZipFile zip = new ZipFile(backup.toFile())) {
            ZipEntry entry = zip.getEntry("mibasedatos.mv.db");
            assertNotNull(entry);
            try (InputStream in = zip.getInputStream(entry)) {
                Files.copy(in, restaurada, StandardCopyOption.REPLACE_EXISTING);
            }
        }

        Files.delete(Path.of(base + ".mv.db"));
        Files.copy(restaurada, Path.of(base + ".mv.db"));

        try (Connection connection = DriverManager.getConnection(url, "sa", "");
                Statement statement = connection.createStatement();
                ResultSet rs = statement.executeQuery("SELECT NOMBRE FROM PRUEBA WHERE ID = 1")) {
            assertTrue(rs.next());
            assertEquals("uno", rs.getString(1));
        }
    }
}
