import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { Observable, finalize } from 'rxjs';
import type { components } from '../../api/api-types';

// Tipos generados desde los controladores por `pnpm api`; no se mantienen a mano.
// `Required` conserva los campos que la UI siempre recibe; `transactionCode` puede ser null.
type BookingDto = components['schemas']['BookingResponseDTO'];
export type IBooking = Omit<Required<BookingDto>, 'transactionCode'> & {
  transactionCode: string | null;
};
export type BookingCreateRequest = Required<
  components['schemas']['BookingWithNewClientRequestDTO']
>;
export type BookingUpdateRequest = Required<components['schemas']['BookingUpdateRequestDTO']>;

@Injectable({
  providedIn: 'root',
})
export class Booking {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  private loadingSignal = signal(false);
  bookingData = signal<IBooking[]>([]);
  bookinglist = this.bookingData.asReadonly();
  loading = this.loadingSignal.asReadonly();

  getBookings(): void {
    this.loadingSignal.set(true);

    this.http
      .get<IBooking[]>(`${this.apiUrl}/bookings`)
      .pipe(finalize(() => this.loadingSignal.set(false)))
      .subscribe({
        next: (data) => this.bookingData.set(data),
        error: (error) => console.error('Error fetching bookings:', error),
      });
  }

  getCurrentDate(): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/bookings/currentdate`);
  }

  createBookingWithClient(booking: BookingCreateRequest): Observable<IBooking> {
    return this.http.post<IBooking>(`${this.apiUrl}/bookings/with-client`, booking);
  }

  deleteBooking(bookingId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/${bookingId}`);
  }

  deleteBookingList(bookingsIds: string[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings`, { body: bookingsIds });
  }

  editBooking(bookingId: string, bookingData: BookingUpdateRequest): Observable<IBooking> {
    return this.http.put<IBooking>(`${this.apiUrl}/bookings/${bookingId}`, bookingData);
  }
}
