import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';
import { LocalTokenService } from './local-token';

@Injectable({
  providedIn: 'root',
})
export class SystemService {
  private url = `${environment.apiUrl}/system/shutdown`;
  private http = inject(HttpClient);
  private localToken = inject(LocalTokenService);

  closeApp(): Observable<void> {
    return this.localToken
      .get()
      .pipe(
        switchMap((token) =>
          this.http.post<void>(this.url, {}, { headers: { 'X-Shutdown-Token': token } }),
        ),
      );
  }
}
