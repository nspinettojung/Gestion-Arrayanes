package com.Gestion.Arrayanes.Config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class SpaResourceResolverTest {

    @TempDir
    Path staticDir;

    private FileUrlResource location;
    private SpaResourceResolver resolver;

    @BeforeEach
    void setUp() throws IOException {
        Files.writeString(staticDir.resolve("index.html"), "<html></html>");
        Files.createDirectories(staticDir.resolve("assets"));
        Files.writeString(staticDir.resolve("assets/app.js"), "console.log('ok')");

        location = new FileUrlResource(staticDir.toUri().toURL());
        resolver = new SpaResourceResolver(
                new FileUrlResource(staticDir.resolve("index.html").toUri().toURL()));
    }

    @Test
    void servesRequestedFile() throws IOException {
        Resource resource = resolver.getResource("assets/app.js", location);

        assertNotNull(resource);
        assertEquals("app.js", resource.getFilename());
    }

    @Test
    void fallsBackToIndexForSpaRoutes() throws IOException {
        Resource resource = resolver.getResource("clientes/123", location);

        assertNotNull(resource);
        assertEquals("index.html", resource.getFilename());
    }

    @Test
    void returnsNullForMissingApiRoute() throws IOException {
        Resource resource = resolver.getResource("api/no-existe", location);

        assertNull(resource);
    }

    @Test
    void returnsNullWhenIndexIsMissing() throws IOException {
        SpaResourceResolver sinIndex = new SpaResourceResolver(
                new FileUrlResource(staticDir.resolve("missing.html").toUri().toURL()));

        assertNull(sinIndex.getResource("clientes", location));
    }
}
