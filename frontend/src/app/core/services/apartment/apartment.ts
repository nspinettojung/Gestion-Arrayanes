import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { Observable, finalize } from 'rxjs';
import type { components } from '../../api/api-types';

// Tipos generados desde los controladores por `pnpm api`; no se mantienen a mano.
export type IApartment = Required<components['schemas']['ApartmentResponseDTO']>;
export type IApartmentRequest = components['schemas']['ApartmentRequestDTO'];

@Injectable({
  providedIn: 'root',
})
export class Apartment {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  private loadingSignal = signal(false);
  apartmentsData = signal<IApartment[]>([]);
  loading = this.loadingSignal.asReadonly();

  getApartments(): void {
    this.loadingSignal.set(true);

    this.http
      .get<IApartment[]>(`${this.apiUrl}/apartments`)
      .pipe(finalize(() => this.loadingSignal.set(false)))
      .subscribe({
        next: (data) => {
          this.apartmentsData.set(data);
        },
        error: (error) => console.error('Error fetching apartments:', error),
      });
  }

  editPrice(id: string, newPrice: number): Observable<IApartment> {
    return this.http.patch<IApartment>(`${this.apiUrl}/apartments/${id}/price`, {
      price: newPrice,
    });
  }

  addApartment(apartment: IApartmentRequest): Observable<IApartment> {
    return this.http.post<IApartment>(`${this.apiUrl}/apartments`, apartment);
  }

  deleteApartment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/apartments/${id}`);
  }

  editApartment(id: string, apartment: IApartmentRequest): Observable<IApartment> {
    return this.http.put<IApartment>(`${this.apiUrl}/apartments/${id}`, apartment);
  }
}
