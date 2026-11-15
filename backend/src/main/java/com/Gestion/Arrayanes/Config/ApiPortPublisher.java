package com.Gestion.Arrayanes.Config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


import org.springframework.boot.web.server.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ApiPortPublisher {

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        int port = event.getWebServer().getPort();

        Path file = Paths.get("api-port.txt");

        try {
            Files.writeString(
                    file,
                    Integer.toString(port),
                    StandardCharsets.UTF_8
            );

            System.out.println(
                    "Puerto publicado para JavaFX: " + port
            );
            System.out.println(
                    "Archivo de puerto: " + file.toAbsolutePath()
            );

        } catch (IOException e) {
            System.err.println(
                    "No se pudo publicar el puerto de la API."
            );
            e.printStackTrace();
        }
    }
}
