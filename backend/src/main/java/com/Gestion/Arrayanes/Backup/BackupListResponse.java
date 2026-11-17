package com.Gestion.Arrayanes.Backup;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/** Listado de copias junto con la que se aplicará en el próximo arranque. */
public record BackupListResponse(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) List<BackupInfo> backups,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, nullable = true) String pendingRestore) {
}
