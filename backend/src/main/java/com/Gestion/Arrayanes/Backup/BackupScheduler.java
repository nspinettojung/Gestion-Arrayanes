package com.Gestion.Arrayanes.Backup;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** Copia diaria automática. Se puede desactivar con `arrayanes.backup.scheduler.enabled=false`. */
@Slf4j
@Component
@ConditionalOnProperty(
        prefix = "arrayanes.backup.scheduler",
        name = "enabled",
        havingValue = "true",
        matchIfMissing = true)
public class BackupScheduler {

    private final BackupService backupService;

    public BackupScheduler(BackupService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "${arrayanes.backup.daily-cron:0 0 3 * * *}")
    public void createDailyBackup() {
        try {
            backupService.createScheduled();
        } catch (Exception e) {
            log.error("No se pudo crear la copia diaria: {}", e.getMessage());
        }
    }
}
