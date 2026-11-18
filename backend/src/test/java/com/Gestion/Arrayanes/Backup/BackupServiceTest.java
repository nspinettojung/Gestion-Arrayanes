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
import java.nio.file.attribute.FileTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
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
    private JdbcTemplate jdbc;
    private String jdbcUrl;
    private Path backups;
    private Path databaseFile;

    @BeforeEach
    void setUp() {
        backups = dir.resolve("backups");
        databaseFile = dir.resolve("mibasedatos.mv.db");
        String path = dir.resolve("mibasedatos").toString().replace('\\', '/');
        jdbcUrl = "jdbc:h2:file:" + path;
        DataSource dataSource = new DriverManagerDataSource(jdbcUrl, "sa", "");
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("CREATE TABLE PRUEBA (ID INT PRIMARY KEY)");
        jdbc.execute("INSERT INTO PRUEBA VALUES (1)");

        service = newService(10);
    }

    private BackupService newService(int maxCopies) {
        return new BackupService(
                backups.toString(), jdbcUrl, maxCopies, jdbc, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private void createFakeZip(int index) throws Exception {
        Files.createDirectories(backups);
        Path file = backups.resolve("backup-manual-20261101-%06d.zip".formatted(index));
        Files.writeString(file, "zip");
        Files.setLastModifiedTime(file, FileTime.fromMillis(index * 1_000L));
    }

    private void createFakeSafetyCopy(int index) throws Exception {
        Path file = dir.resolve("mibasedatos.mv.db.bak-20261101-%06d".formatted(index));
        Files.writeString(file, "bak");
        Files.setLastModifiedTime(file, FileTime.fromMillis(index * 1_000L));
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

    @Test
    void conservaSoloLasUltimasDiezCopias() throws Exception {
        for (int i = 1; i <= 12; i++) {
            createFakeZip(i);
        }

        BackupInfo created = service.createManual();

        List<String> names = service.list().backups().stream().map(BackupInfo::name).toList();
        assertEquals(10, names.size());
        assertFalse(Files.exists(backups.resolve("backup-manual-20261101-000001.zip")));
        assertTrue(Files.exists(backups.resolve("backup-manual-20261101-000012.zip")));
        assertTrue(names.contains(created.name()));
    }

    @Test
    void noBorraLaCopiaPendienteDeRestauracion() throws Exception {
        for (int i = 1; i <= 12; i++) {
            createFakeZip(i);
        }
        String pending = "backup-manual-20261101-000001.zip";
        Files.writeString(backups.resolve(BackupFiles.MARKER), pending);

        service.createManual();

        assertTrue(Files.exists(backups.resolve(pending)));
        assertEquals(pending, service.list().pendingRestore());
        assertEquals(11, service.list().backups().size());
    }

    @Test
    void conservaSoloDiezCopiasDeSeguridadPreviasALaRestauracion() throws Exception {
        for (int i = 1; i <= 12; i++) {
            createFakeSafetyCopy(i);
        }

        service.createManual();

        long remaining;
        try (var files = Files.list(dir)) {
            remaining = files.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("mibasedatos.mv.db.bak-"))
                    .count();
        }
        assertEquals(10, remaining);
        assertFalse(Files.exists(dir.resolve("mibasedatos.mv.db.bak-20261101-000001")));
        assertTrue(Files.exists(dir.resolve("mibasedatos.mv.db.bak-20261101-000012")));
    }

    @Test
    void respetaElLimiteConfigurado() throws Exception {
        for (int i = 1; i <= 5; i++) {
            createFakeZip(i);
        }

        BackupInfo created = newService(3).createManual();

        List<String> names = service.list().backups().stream().map(BackupInfo::name).toList();
        assertEquals(3, names.size());
        assertTrue(names.contains(created.name()));
    }
}
