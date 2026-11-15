import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';

export interface BuildInfo {
  version: string;
  commit: string;
  branch: string;
  dirty: boolean;
  builtAt: string;
}

@Injectable({
  providedIn: 'root',
})
export class BuildInfoService {
  private http = inject(HttpClient);
  private readonly info = signal<BuildInfo | null>(null);

  readonly buildInfo = this.info.asReadonly();
  readonly label = computed(() => {
    const info = this.info();
    if (!info) {
      return '';
    }
    return `v${info.version} · ${info.commit}${info.dirty ? ' *' : ''}`;
  });
  readonly builtAtLabel = computed(() => {
    const info = this.info();
    if (!info) {
      return '';
    }
    const date = new Date(info.builtAt);
    if (Number.isNaN(date.getTime())) {
      return '';
    }
    return `Build ${date.toLocaleString('es-AR', { dateStyle: 'short', timeStyle: 'short' })}`;
  });

  load(): void {
    if (this.info()) {
      return;
    }
    this.http.get<BuildInfo>('/build-info.json').subscribe({
      next: (info) => this.info.set(info),
      error: () => {
        // En `ng serve` el archivo puede no existir todavía: se oculta la versión.
      },
    });
  }
}
