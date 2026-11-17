import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import type { components } from '../../api/api-types';

/** Token local de un solo uso por arranque, reutilizado por apagado y copias. */
@Injectable({
  providedIn: 'root',
})
export class LocalTokenService {
  private readonly url = `${environment.apiUrl}/system/shutdown-token`;
  private http = inject(HttpClient);

  get(): Observable<string> {
    return this.http
      .get<components['schemas']['ShutdownTokenResponse']>(this.url)
      .pipe(map((response) => response.token));
  }
}
