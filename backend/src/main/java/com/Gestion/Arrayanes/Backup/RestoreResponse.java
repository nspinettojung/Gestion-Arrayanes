package com.Gestion.Arrayanes.Backup;

/** Confirmación de que una copia quedó marcada para restaurar al reiniciar. */
public record RestoreResponse(String message, String backupName) {
}
