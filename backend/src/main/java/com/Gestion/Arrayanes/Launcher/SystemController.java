package com.Gestion.Arrayanes.Launcher;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/system")
public class SystemController {

    @PostMapping("/shutdown")
    public ResponseEntity<String> shutdownApp() {
        // Respondemos al cliente HTTP antes de que el servidor se apague
        ResponseEntity<String> response = ResponseEntity.ok("La aplicación se está cerrando...");

        // Usamos un hilo separado (CompletableFuture) con un pequeño delay
        // para asegurar que el navegador alcance a recibir la respuesta 200 OK antes de morir.
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000); // 1 segundo de espera
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            // Mata el proceso completo de Spring Boot y libera el puerto
            System.exit(0);
        });

        return response;
    }
}
