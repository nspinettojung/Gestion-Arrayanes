package com.Gestion.Arrayanes.Backup;

import java.util.List;

/** Listado de copias junto con la que se aplicará en el próximo arranque. */
public record BackupListResponse(List<BackupInfo> backups, String pendingRestore) {
}
