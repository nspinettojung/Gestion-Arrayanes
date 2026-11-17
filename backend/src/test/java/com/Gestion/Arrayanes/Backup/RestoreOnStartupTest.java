package com.Gestion.Arrayanes.Backup;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RestoreOnStartupTest {

    private static final String BACKUP_NAME = "backup-manual-20261117-030000.zip";

    @TempDir
    Path dir;

    private String databaseBase;
    private Path databaseFile;
    private Path backups;
    private Path marker;

    @BeforeEach
    void setUp() {
        databaseBase = dir.resolve("mibasedatos").toString().replace('\\', '/');
        databaseFile = Path.of(databaseBase + ".mv.db");
        backups = dir.resolve("backups");
        marker = backups.resolve(BackupFiles.MARKER);
    }

    @Test
    void noHaceNadaSiNoHayUnaCopiaPendiente() {
        RestoreOnStartup.RestoreResult result = RestoreOnStartup.restore(backups, databaseFile);

        assertFalse(result.applied());
        assertNull(result.backupName());
        assertNull(result.error());
    }

    @Test
    void restauraLaCopiaYDejaUnRespaldoDeLaBaseActual() throws Exception {
        Files.createDirectories(backups);
        createDatabase("original");
        createBackup();
        updateValue("modificado");
        assertEquals("modificado", readValue());
        Files.writeString(marker, BACKUP_NAME, StandardCharsets.UTF_8);

        RestoreOnStartup.RestoreResult result = RestoreOnStartup.restore(backups, databaseFile);

        assertTrue(result.applied(), String.valueOf(result.error()));
        assertEquals(BACKUP_NAME, result.backupName());
        assertNull(result.error());
        assertFalse(Files.exists(marker));
        assertEquals("original", readValue());
        assertTrue(Files.list(dir).anyMatch(path -> path.getFileName()
                .toString()
                .startsWith("mibasedatos.mv.db.bak-")));
    }

    @Test
    void conservaElMarcadorSiElNombreNoEsValido() throws Exception {
        Files.createDirectories(backups);
        createDatabase("original");
        Files.writeString(marker, "../mibasedatos.mv.db", StandardCharsets.UTF_8);

        RestoreOnStartup.RestoreResult result = RestoreOnStartup.restore(backups, databaseFile);

        assertFalse(result.applied());
        assertTrue(result.error().contains("no es válido"));
        assertTrue(Files.exists(marker));
        assertEquals("original", readValue());
    }

    @Test
    void conservaElMarcadorSiNoExisteLaCopia() throws Exception {
        Files.createDirectories(backups);
        createDatabase("original");
        Files.writeString(marker, BACKUP_NAME, StandardCharsets.UTF_8);

        RestoreOnStartup.RestoreResult result = RestoreOnStartup.restore(backups, databaseFile);

        assertFalse(result.applied());
        assertTrue(result.error().contains("No se encontró"));
        assertTrue(Files.exists(marker));
        assertEquals("original", readValue());
    }

    private void createDatabase(String value) throws SQLException {
        try (Connection connection = connection();
                Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE PRUEBA (VALOR VARCHAR(20))");
            statement.execute("INSERT INTO PRUEBA VALUES ('" + value + "')");
        }
    }

    private void createBackup() throws SQLException {
        try (Connection connection = connection();
                Statement statement = connection.createStatement()) {
            String target = backups.resolve(BACKUP_NAME).toString().replace('\\', '/');
            statement.execute("BACKUP TO '" + target + "'");
        }
    }

    private void updateValue(String value) throws SQLException {
        try (Connection connection = connection();
                Statement statement = connection.createStatement()) {
            statement.execute("UPDATE PRUEBA SET VALOR = '" + value + "'");
        }
    }

    private String readValue() throws SQLException {
        try (Connection connection = connection();
                Statement statement = connection.createStatement();
                ResultSet result = statement.executeQuery("SELECT VALOR FROM PRUEBA")) {
            assertTrue(result.next());
            return result.getString(1);
        }
    }

    private Connection connection() throws SQLException {
        return DriverManager.getConnection("jdbc:h2:file:" + databaseBase, "sa", "");
    }
}
