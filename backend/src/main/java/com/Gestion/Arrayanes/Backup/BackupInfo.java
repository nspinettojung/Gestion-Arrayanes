package com.Gestion.Arrayanes.Backup;

import java.time.Instant;

/** Datos que la UI muestra para cada copia de seguridad. */
public record BackupInfo(String name, String kind, Instant createdAt, long sizeBytes, boolean pending) {
}
