package com.Gestion.Arrayanes.Backup;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.zip.ZipFile;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BackupServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-17T03:00:00Z");

    @TempDir
    Path dir;

    private BackupService service;
    private Path backups;
    private Path databaseFile;

    @BeforeEach
    void setUp() {
        backups = dir.resolve("backups");
        databaseFile = dir.resolve("mibasedatos.mv.db");
        String path = dir.resolve("mibasedatos").toString().replace('\\', '/');
        DataSource dataSource = new DriverManagerDataSource("jdbc:h2:file:" + path, "sa", "");
        JdbcTemplate jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE PRUEBA (ID INT PRIMARY KEY)");
        jdbc.execute("INSERT INTO PRUEBA VALUES (1)");

        service = new BackupService(
                backups.toString(), "jdbc:h2:file:" + path, jdbc, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void creaUnZipConElArchivoDeBase() throws Exception {
        BackupInfo info = service.createManual();

        assertEquals("manual", info.kind());
        assertEquals(NOW, info.createdAt());
        assertTrue(info.name().matches("backup-manual-\\d{8}-\\d{6}\\.zip"));
        assertFalse(info.pending());
        assertTrue(info.sizeBytes() > 0);

        try (ZipFile zip = new ZipFile(backups.resolve(info.name()).toFile())) {
            assertNotNull(zip.getEntry(BackupFiles.backupEntryName(databaseFile)));
        }
    }

    @Test
    void listaLasCopiasDeMasRecienteAMasAntigua() throws Exception {
        service.createManual();
        service.createOnShutdown();

        BackupListResponse response = service.list();

        assertEquals(2, response.backups().size());
        assertTrue(response.backups().stream().anyMatch(backup -> backup.kind().equals("cierre")));
        assertTrue(response.backups().stream().anyMatch(backup -> backup.kind().equals("manual")));
        assertNull(response.pendingRestore());
    }

    @Test
    void marcaUnaCopiaParaRestaurarAlReiniciar() throws Exception {
        BackupInfo created = service.createManual();

        BackupInfo marked = service.requestRestore(created.name());

        assertTrue(marked.pending());
        BackupListResponse response = service.list();
        assertEquals(created.name(), response.pendingRestore());
        assertTrue(response.backups().get(0).pending());
        assertEquals(created.name(), Files.readString(backups.resolve(BackupFiles.MARKER)).trim());
    }

    @Test
    void evitaDuplicarElNombreSiHayDosCopiasEnElMismoSegundo() throws Exception {
        BackupInfo first = service.createManual();
        BackupInfo second = service.createManual();

        assertTrue(second.name().startsWith(first.name().replace(".zip", "")));
        assertEquals(2, service.list().backups().size());
    }

    @Test
    void rechazaNombresConRutas() {
        assertThrows(BadRequestException.class, () -> service.requestRestore("../mibasedatos.mv.db"));
        assertThrows(BadRequestException.class, () -> service.requestRestore("backup.zip/../otro.zip"));
        assertThrows(BadRequestException.class, () -> service.requestRestore(""));
    }

    @Test
    void rechazaUnaCopiaQueNoExiste() {
        assertThrows(
                ResourceNotFoundException.class,
                () -> service.requestRestore("backup-manual-20200101-000000.zip"));
    }
}
