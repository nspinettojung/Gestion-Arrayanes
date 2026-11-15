import { inject, Injectable } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class SystemService {
  private url = `${environment.apiUrl}/system/shutdown`;
  private http = inject(HttpClient);

  closeApp(): Observable<any> {
    return this.http.post(this.url, {});
  }
}
