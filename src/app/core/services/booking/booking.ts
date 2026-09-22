import { HttpClient } from '@angular/common/http';
import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { BookingCreateRequest, IBooking } from '../../interface/booking/booking';
import { Observable } from 'rxjs';
import { CalendarDayDTO } from '../../interface/booking/calendar-day-dto';

interface CalendarDay {
  dayNumber: number | null;
  isCurrentMonth: boolean;
  hasMaintenance?: boolean;
  reservation?: string;
}

@Injectable({
  providedIn: 'root',
})
export class Booking {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  bookingData = signal<IBooking[]>([]);
  bookinglist = this.bookingData.asReadonly();
  days = signal<CalendarDay[]>([]);

  getBookings(): void {
    this.http.get<IBooking[]>(`${this.apiUrl}/bookings/bookinglist`).subscribe({
      next: (data) => this.bookingData.set(data),
      error: (error) => console.error('Error fetching bookings:', error),
    });
  }

  getCurrentDate(): Observable<string> {
    return this.http.get<string>(`${this.apiUrl}/bookings/currentdate`);
  }

  getCalendar(year: number, month: number): void {
    this.http
      .get<CalendarDayDTO[]>(`${this.apiUrl}/dashboard/calendar?year=${year}&month=${month}`)
      .subscribe({
        next: (data) => {
          const calendarGrid: CalendarDay[] = [];

          // convertir respuesta en estructura para el grid
          data.forEach((dto) => {
            const dayNumber = new Date(dto.date).getDate();
            calendarGrid.push({
              dayNumber,
              isCurrentMonth: true,
              reservation: dto.hasBooking ? 'Reservado' : undefined,
              hasMaintenance: false, // opcional, si querés agregar otra lógica
            });
          });

          this.days.set(calendarGrid);
        },
        error: (error) => console.error('Error fetching calendar:', error),
      });
  }

  createBookingWithClient(booking: BookingCreateRequest): Observable<IBooking> {
    return this.http.post<IBooking>(`${this.apiUrl}/bookings/createWithClient`, booking);
  }

  generateTransactionCode(): Observable<string> {
    return this.http.get(`${this.apiUrl}/bookings/generateTransactionCode`, {
      responseType: 'text',
    });
  }

  deleteBooking(bookingId: string): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings/delete/${bookingId}`);
  }

  deleteBookingList(bookingsIds: string[]): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/bookings`, { body: bookingsIds });
  }

  editBooking(bookingId: string, bookingData: IBooking): Observable<IBooking> {
    return this.http.put<IBooking>(`${this.apiUrl}/bookings/editbooking/${bookingId}`, bookingData);
  }
}
