package com.Gestion.Arrayanes.Launcher;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SystemController.class)
@Import(ShutdownToken.class)
@TestPropertySource(properties = "arrayanes.shutdown.token=token-de-prueba")
class SystemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ShutdownInvoker shutdownInvoker;

    @Test
    void entregaElTokenAlMismoOrigen() throws Exception {
        mockMvc.perform(get("/api/v1/system/shutdown-token").header("Sec-Fetch-Site", "same-origin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token-de-prueba"));
    }

    @Test
    void noEntregaElTokenASitiosAjenos() throws Exception {
        mockMvc.perform(get("/api/v1/system/shutdown-token").header("Sec-Fetch-Site", "cross-site"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    void apagaConElTokenCorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/system/shutdown")
                        .header(SystemController.TOKEN_HEADER, "token-de-prueba"))
                .andExpect(status().isOk())
                .andExpect(content().string("La aplicación se está cerrando..."));

        verify(shutdownInvoker).schedule();
    }

    @Test
    void rechazaElApagadoSinToken() throws Exception {
        mockMvc.perform(post("/api/v1/system/shutdown"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.detail").value("Token de apagado inválido."));

        verify(shutdownInvoker, never()).schedule();
    }

    @Test
    void rechazaElApagadoConTokenIncorrecto() throws Exception {
        mockMvc.perform(post("/api/v1/system/shutdown")
                        .header(SystemController.TOKEN_HEADER, "otro-token"))
                .andExpect(status().isForbidden());

        verify(shutdownInvoker, never()).schedule();
    }
}
