package com.Gestion.Arrayanes.Config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Genera el contrato HTTP desde los controladores reales. El script raíz `pnpm api` copia este
 * archivo al frontend y genera los tipos TypeScript a partir de él. Las claves se ordenan porque
 * la reflexión de Spring Data no garantiza el orden de las propiedades de paginación.
 */
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiSpecTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    private MockMvc mockMvc;

    @Test
    void escribeElEspecificacionAbierta() throws Exception {
        String spec = mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode canonico = ordenar(mapper.readTree(spec));
        String pretty = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(canonico);
        Files.writeString(Path.of("target", "openapi.json"), pretty);
    }

    private JsonNode ordenar(JsonNode nodo) {
        if (nodo.isObject()) {
            ObjectNode salida = mapper.createObjectNode();
            List<String> claves = new ArrayList<>();
            nodo.fieldNames().forEachRemaining(claves::add);
            Collections.sort(claves);
            for (String clave : claves) {
                salida.set(clave, ordenar(nodo.get(clave)));
            }
            return salida;
        }
        if (nodo.isArray()) {
            ArrayNode salida = mapper.createArrayNode();
            nodo.forEach(elemento -> salida.add(ordenar(elemento)));
            return salida;
        }
        return nodo;
    }
}
