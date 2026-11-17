package com.Gestion.Arrayanes.Backup;

import io.swagger.v3.oas.annotations.media.Schema;

/** Confirmación de que una copia quedó marcada para restaurar al reiniciar. */
public record RestoreResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String message,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String backupName) {
}
