package com.Gestion.Arrayanes.Launcher;

import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/** Programa el apagado del proceso después de haber respondido al cliente. */
@Component
public class ShutdownInvoker {

    public void schedule() {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            System.exit(0);
        });
    }
}
