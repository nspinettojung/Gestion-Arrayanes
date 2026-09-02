import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { IApartment } from '../../interface/apartment/apartment';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Apartment {
  private http = inject(HttpClient);
  private apiUrl = environment.apiUrl;
  apartmentsData = signal<IApartment[]>([]);

  getApartments(): void {
    this.http.get<IApartment[]>(`${this.apiUrl}/apartments/listapartment`).subscribe({
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

  addApartment(apartment: IApartment): Observable<IApartment> {
    return this.http.post<IApartment>(`${this.apiUrl}/apartments/saveapartment`, apartment);
  }

  deleteApartment(id: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/apartments/delete/${id}`);
  }
}
