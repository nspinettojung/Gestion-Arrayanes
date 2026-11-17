import { Component, inject, OnInit, signal } from '@angular/core';
import { DatePipe } from '@angular/common';
import { finalize } from 'rxjs';
import { BackupInfo, BackupService } from '../../core/services/backup/backup';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { NotificationService } from '../../core/services/notification/notification';
import { SystemService } from '../../core/services/system/system-service';

@Component({
  selector: 'app-mainbackups',
  imports: [DatePipe],
  templateUrl: './mainbackups.html',
  styleUrl: './mainbackups.scss',
})
export class Mainbackups implements OnInit {
  private backupService = inject(BackupService);
  private systemService = inject(SystemService);
  private confirmation = inject(ConfirmationService);
  private notification = inject(NotificationService);

  readonly backups = signal<BackupInfo[]>([]);
  readonly pendingRestore = signal<string | null>(null);
  readonly loading = signal(false);
  readonly working = signal(false);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.backupService
      .list()
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: (response) => {
          this.backups.set(response.backups);
          this.pendingRestore.set(response.pendingRestore);
        },
        error: () => this.notification.error('No se pudieron cargar las copias de seguridad.'),
      });
  }

  create(): void {
    this.working.set(true);
    this.backupService
      .create()
      .pipe(finalize(() => this.working.set(false)))
      .subscribe({
        next: (backup) => {
          this.notification.success(`Copia "${backup.name}" creada.`);
          this.load();
        },
        error: () => this.notification.error('No se pudo crear la copia de seguridad.'),
      });
  }

  async restore(backup: BackupInfo): Promise<void> {
    const accepted = await this.confirmation.confirm({
      title: 'Restaurar copia',
      message: `Se restaurará "${backup.name}" al volver a abrir la aplicación. Se guardará una copia de la base actual y luego se cerrará la app. ¿Continuar?`,
      confirmText: 'Restaurar y cerrar',
      danger: true,
    });
    if (!accepted) {
      return;
    }

    this.working.set(true);
    this.backupService
      .restore(backup.name)
      .pipe(finalize(() => this.working.set(false)))
      .subscribe({
        next: (response) => {
          this.notification.info(response.message);
          this.closeApp();
        },
        error: () => this.notification.error('No se pudo marcar la copia para restaurar.'),
      });
  }

  kindLabel(kind: string): string {
    switch (kind) {
      case 'diario':
        return 'Diaria';
      case 'cierre':
        return 'Al cerrar';
      case 'manual':
        return 'Manual';
      default:
        return kind;
    }
  }

  sizeLabel(bytes: number): string {
    if (bytes < 1024) {
      return `${bytes} B`;
    }
    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }
    return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
  }

  private closeApp(): void {
    this.systemService.closeApp().subscribe({
      error: () =>
        this.notification.error('La copia quedó marcada, pero no se pudo cerrar la aplicación.'),
    });
  }
}
