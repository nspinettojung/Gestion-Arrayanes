package com.Gestion.Arrayanes.Backup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

/** Datos que la UI muestra para cada copia de seguridad. */
public record BackupInfo(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String kind,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Instant createdAt,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) long sizeBytes,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) boolean pending) {
}
