import { inject, Injectable, signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { IDashboard } from '../../interface/dashboard/dashboard';
import { ClientApartmentDTO } from '../../interface/clients/client-apartment-dto';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Dashboard {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  totalBookings = signal<number>(0);
  totalApartments = signal<number>(0);
  todayBookings = signal<number>(0);
  occupancy = signal<number>(0);
  growth = signal<number>(0);
  monthlyArrivals = signal<number>(0);
  monthlyDepartures = signal<number>(0);
  arrivalsCount = signal<number>(0);
  departuresCount = signal<number>(0);
  monthlyArrivalsList = signal<IDashboard[]>([]);
  monthlyDeparturesList = signal<IDashboard[]>([]);
  arrivalsByDate = signal<ClientApartmentDTO[]>([]);
  departuresByDate = signal<ClientApartmentDTO[]>([]);

  dashboardSignal = signal<IDashboard[]>([]);

  loadDashboard(page = 0, size = 10): void {
    this.http
      .get<any>(`${this.apiUrl}/dashboard/filter`, {
        params: {
          page,
          size,
          sortBy: 'arrivalDate',
          sortDir: 'asc',
        },
      })
      .subscribe({
        next: (data) => this.dashboardSignal.set(data.content),
        error: (err) => console.error('❌ Error fetching dashboard:', err),
      });
  }

  getDashboardData(): void {
    this.http.get<IDashboard[]>(`${this.apiUrl}/dashboard`).subscribe({
      next: (data) => this.dashboardSignal.set(data),
      error: (err) => console.error('❌ Error fetching dashboard data:', err),
    });
  }

  getTotal(entity: 'bookings' | 'apartments'): void {
    this.http.get<{ total: number }>(`${this.apiUrl}/dashboard/total/${entity}`).subscribe({
      next: (data) => {
        if (entity === 'bookings') this.totalBookings.set(data.total);
        if (entity === 'apartments') this.totalApartments.set(data.total);
      },
      error: (error) => console.error(`Error fetching total ${entity}:`, error),
    });
  }

  getOccupancy(): void {
    this.http.get<number>(`${this.apiUrl}/dashboard/occupancy`).subscribe({
      next: (data) => {
        this.occupancy.set(data);
      },
      error: (error) => console.error('Error fetching occupancy data:', error),
    });
  }

  getBookingsGrowth(): void {
    this.http.get<number>(`${this.apiUrl}/dashboard/bookingsgrowth`).subscribe({
      next: (data) => {
        this.growth.set(data);
      },
      error: (error) => console.error('Error fetching bookings growth data:', error),
    });
  }

  getTodayBookings(): void {
    this.http.get<number>(`${this.apiUrl}/dashboard/todaybookings`).subscribe({
      next: (data) => {
        this.todayBookings.set(data);
      },
      error: (error) => console.error("Error fetching today's bookings data:", error),
    });
  }

  getMonthlyArrivals(month: number, year: number): void {
    this.http
      .get<number>(`${this.apiUrl}/dashboard/monthlyArrivalsCount?month=${month}&year=${year}`)
      .subscribe({
        next: (data) => {
          this.monthlyArrivals.set(data);
        },
        error: (error) => console.error('Error fetching monthly arrivals:', error),
      });
  }

  getMonthlyDepartures(month: number, year: number): void {
    this.http
      .get<number>(`${this.apiUrl}/dashboard/monthlyDeparturesCount?month=${month}&year=${year}`)
      .subscribe({
        next: (data) => {
          this.monthlyDepartures.set(data);
        },
        error: (error) => console.error('Error fetching monthly departures:', error),
      });
  }

  getMonthlyArrivalsList(month: number, year: number): Observable<IDashboard[]> {
    return this.http.get<IDashboard[]>(
      `${this.apiUrl}/dashboard/monthlyArrivalsList?month=${month}&year=${year}`,
    );
  }

  getMonthlyDeparturesList(month: number, year: number): Observable<IDashboard[]> {
    return this.http.get<IDashboard[]>(
      `${this.apiUrl}/dashboard/monthlyDeparturesList?month=${month}&year=${year}`,
    );
  }

  getArrivalsByDate(year: number, month: number, day: number): void {
    this.http
      .get<
        ClientApartmentDTO[]
      >(`${this.apiUrl}/dashboard/arrivalsByDate?year=${year}&month=${month}&day=${day}`)
      .subscribe({
        next: (data) => {
          this.arrivalsByDate.set(data);
        },
        error: (error) => console.error('Error fetching arrivals by date:', error),
      });
  }

  getDeparturesByDate(year: number, month: number, day: number): void {
    this.http
      .get<
        ClientApartmentDTO[]
      >(`${this.apiUrl}/dashboard/departuresByDate?year=${year}&month=${month}&day=${day}`)
      .subscribe({
        next: (data) => {
          this.departuresByDate.set(data);
        },
        error: (error) => console.error('Error fetching departures by date:', error),
      });
  }

  getArrivalsCountByDate(year: number, month: number, day: number): void {
    this.http
      .get<number>(
        `${this.apiUrl}/dashboard/arrivalsCountByDate?year=${year}&month=${month}&day=${day}`,
      )
      .subscribe({
        next: (count) => this.arrivalsCount.set(count),
        error: (err) => console.error('Error fetching arrivals count:', err),
      });
  }

  getDeparturesCountByDate(year: number, month: number, day: number): void {
    this.http
      .get<number>(
        `${this.apiUrl}/dashboard/departuresCountByDate?year=${year}&month=${month}&day=${day}`,
      )
      .subscribe({
        next: (count) => this.departuresCount.set(count),
        error: (err) => console.error('Error fetching departures count:', err),
      });
  }
}
