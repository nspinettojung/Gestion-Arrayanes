package com.Gestion.Arrayanes.Launcher;

import com.Gestion.Arrayanes.Backup.BackupService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/** Programa el apagado del proceso y guarda una copia antes de terminar. */
@Slf4j
@Component
public class ShutdownInvoker {

    private final BackupService backupService;

    public ShutdownInvoker(BackupService backupService) {
        this.backupService = backupService;
    }

    /** Espera a que el cliente reciba la respuesta HTTP antes de apagar. */
    public void schedule() {
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            shutdown();
        });
    }

    /** Apagado usado también cuando se cierra la ventana del navegador. */
    public void shutdown() {
        try {
            backupService.createOnShutdown();
        } catch (Exception e) {
            log.error("No se pudo crear la copia al cerrar: {}", e.getMessage());
        }
        System.exit(0);
    }
}
