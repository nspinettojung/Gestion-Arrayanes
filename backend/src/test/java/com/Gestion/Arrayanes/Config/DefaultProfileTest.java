package com.Gestion.Arrayanes.Config;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** El jar empaquetado debe arrancar como escritorio sin pasar ningún perfil. */
class DefaultProfileTest {

    @Test
    void elPerfilPorDefectoEsProduccion() throws IOException {
        assertEquals("prod", leer("application.properties").getProperty("spring.profiles.default"));
    }

    @Test
    void elPerfilProdActivaElModoEscritorio() throws IOException {
        assertEquals("true", leer("application-prod.properties").getProperty("arrayanes.desktop.enabled"));
    }

    @Test
    void elPerfilProdDesactivaLaConsolaH2() throws IOException {
        Properties prod = leer("application-prod.properties");
        assertEquals("false", prod.getProperty("spring.h2.console.enabled"));
        assertEquals("false", prod.getProperty("spring.jpa.show-sql"));
    }

    @Test
    void elPerfilDevMantieneLaConsolaH2EnTestdb() throws IOException {
        Properties dev = leer("application-dev.properties");
        assertEquals("true", dev.getProperty("spring.h2.console.enabled"));
        assertEquals("/testdb", dev.getProperty("spring.h2.console.path"));
    }

    private Properties leer(String nombre) throws IOException {
        Properties properties = new Properties();
        Path archivo = Path.of("src", "main", "resources", nombre);
        try (var reader = Files.newBufferedReader(archivo)) {
            properties.load(reader);
        }
        return properties;
    }
}
