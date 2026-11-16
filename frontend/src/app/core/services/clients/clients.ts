import { inject, Injectable, signal } from '@angular/core';
import { IClients } from '../../interface/clients/clients';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { Observable, finalize, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Clients {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  private clientsData = signal<IClients[]>([]);
  private loadingSignal = signal(false);

  clientslist = this.clientsData.asReadonly();
  loading = this.loadingSignal.asReadonly();

  getClients(): void {
    this.loadingSignal.set(true);

    this.http
      .get<IClients[]>(`${this.apiUrl}/clients`)
      .pipe(finalize(() => this.loadingSignal.set(false)))
      .subscribe({
        next: (data) => {
          this.clientsData.set(data);
        },
        error: (error) => console.error('Error fetching clients data:', error),
      });
  }

  createClient(client: {
    clientName: string;
    clientLastName: string;
    clientDni: string;
    clientPhone: string;
  }): Observable<IClients> {
    return this.http.post<IClients>(`${this.apiUrl}/clients`, client);
  }

  deleteClient(clientId: string) {
    return this.http.delete<void>(`${this.apiUrl}/clients/${clientId}`).pipe(
      tap(() => {
        this.clientsData.update((list) => list.filter((client) => client.id !== clientId));
      }),
    );
  }

  editClient(clientId: string, clientData: IClients): Observable<IClients> {
    return this.http.put<IClients>(`${this.apiUrl}/clients/${clientId}`, clientData).pipe(
      tap((updatedClient) => {
        this.clientsData.update((list) =>
          list.map((client) => (client.id === clientId ? updatedClient : client)),
        );
      }),
    );
  }
}
