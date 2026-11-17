package com.Gestion.Arrayanes.Backup;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.Launcher.ShutdownToken;
import com.Gestion.Arrayanes.Launcher.SystemController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BackupController.class)
@Import(ShutdownToken.class)
@TestPropertySource(properties = "arrayanes.shutdown.token=token-de-prueba")
class BackupControllerTest {

    private static final String URL = "/api/v1/system/backups";
    private static final String TOKEN = SystemController.TOKEN_HEADER;

    private static final BackupInfo INFO = new BackupInfo(
            "backup-manual-20261117-030000.zip",
            "manual",
            Instant.parse("2026-11-17T03:00:00Z"),
            2048,
            false);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BackupService backupService;

    @Test
    void requiereTokenParaListar() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isForbidden());

        verifyNoInteractions(backupService);
    }

    @Test
    void listaLasCopiasConElToken() throws Exception {
        when(backupService.list()).thenReturn(new BackupListResponse(List.of(INFO), null));

        mockMvc.perform(get(URL).header(TOKEN, "token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backups.length()").value(1))
                .andExpect(jsonPath("$.backups[0].name").value(INFO.name()))
                .andExpect(jsonPath("$.backups[0].pending").value(false));
    }

    @Test
    void creaUnaCopiaConElToken() throws Exception {
        when(backupService.createManual()).thenReturn(INFO);

        mockMvc.perform(post(URL).header(TOKEN, "token-de-prueba"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(INFO.name()));

        verify(backupService).createManual();
    }

    @Test
    void marcaUnaCopiaParaRestaurar() throws Exception {
        when(backupService.requestRestore(INFO.name()))
                .thenReturn(new BackupInfo(INFO.name(), "manual", INFO.createdAt(), INFO.sizeBytes(), true));

        mockMvc.perform(post(URL + "/" + INFO.name() + "/restore").header(TOKEN, "token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.backupName").value(INFO.name()))
                .andExpect(jsonPath("$.message").value("La copia se restaurará al reiniciar la aplicación."));
    }

    @Test
    void rechazaUnaCopiaSinToken() throws Exception {
        mockMvc.perform(post(URL + "/" + INFO.name() + "/restore"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));

        verifyNoInteractions(backupService);
    }

    @Test
    void devuelve400SiElNombreNoEsValido() throws Exception {
        doThrow(new BadRequestException("El nombre de la copia no es válido."))
                .when(backupService)
                .requestRestore(anyString());

        mockMvc.perform(post(URL + "/..%2Fmibasedatos.mv.db/restore").header(TOKEN, "token-de-prueba"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("El nombre de la copia no es válido."));
    }
}
