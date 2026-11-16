package com.Gestion.Arrayanes.Config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SpaConfigurationTest.DummyController.class)
@Import(SpaConfiguration.class)
class SpaConfigurationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void apiRouteShouldReturnJsonNotFoundInsteadOfSpaFallback() throws Exception {
        mockMvc.perform(get("/api/loquesea"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("El recurso solicitado no existe."));
    }

    @Test
    void spaRouteShouldFallbackToIndexHtml() throws Exception {
        mockMvc.perform(get("/reservas/123"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    void staticAssetShouldBeServed() throws Exception {
        mockMvc.perform(get("/build-info.json"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"));
    }

    @RestController
    static class DummyController {
        @GetMapping("/dummy-ping")
        String ping() {
            return "pong";
        }
    }
}
