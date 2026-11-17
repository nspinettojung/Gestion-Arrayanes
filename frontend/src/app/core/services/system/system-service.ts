import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SystemService {
  private url = `${environment.apiUrl}/system/shutdown`;
  private tokenUrl = `${environment.apiUrl}/system/shutdown-token`;
  private http = inject(HttpClient);

  closeApp(): Observable<void> {
    return this.http
      .get<{ token: string }>(this.tokenUrl)
      .pipe(
        switchMap(({ token }) =>
          this.http.post<void>(this.url, {}, { headers: { 'X-Shutdown-Token': token } }),
        ),
      );
  }
}
