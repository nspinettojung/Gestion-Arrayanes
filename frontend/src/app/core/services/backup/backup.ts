import { inject, Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { components } from '../../api/api-types';
import { LocalTokenService } from '../system/local-token';

// Tipos generados desde los controladores por `pnpm api`; no se mantienen a mano.
export type BackupInfo = components['schemas']['BackupInfo'];
export type BackupListResponse = components['schemas']['BackupListResponse'];
export type RestoreResponse = components['schemas']['RestoreResponse'];

@Injectable({
  providedIn: 'root',
})
export class BackupService {
  private readonly url = `${environment.apiUrl}/system/backups`;
  private http = inject(HttpClient);
  private localToken = inject(LocalTokenService);

  list(): Observable<BackupListResponse> {
    return this.withToken((headers) => this.http.get<BackupListResponse>(this.url, { headers }));
  }

  create(): Observable<BackupInfo> {
    return this.withToken((headers) => this.http.post<BackupInfo>(this.url, {}, { headers }));
  }

  restore(name: string): Observable<RestoreResponse> {
    return this.withToken((headers) =>
      this.http.post<RestoreResponse>(
        `${this.url}/${encodeURIComponent(name)}/restore`,
        {},
        { headers },
      ),
    );
  }

  private withToken<T>(request: (headers: HttpHeaders) => Observable<T>): Observable<T> {
    return this.localToken
      .get()
      .pipe(switchMap((token) => request(new HttpHeaders({ 'X-Shutdown-Token': token }))));
  }
}
