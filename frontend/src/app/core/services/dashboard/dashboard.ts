import { inject, Injectable, signal, computed, Signal, WritableSignal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient } from '@angular/common/http';
import { IDashboard } from '../../interface/dashboard/dashboard';
import { DashboardStats } from '../../interface/dashboard/dashboard-stats';
import { CalendarDay } from '../../interface/dashboard/calendar-day';
import { ClientApartmentDTO } from '../../interface/clients/client-apartment-dto';
import { Observable, finalize } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class Dashboard {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);
  private loadingStatsSignal = signal(false);
  loadingStats = this.loadingStatsSignal.asReadonly();
  totalBookings = signal<number>(0);
  totalApartments = signal<number>(0);
  todayBookings = signal<number>(0);
  occupancy = signal<number>(0);
  growth = signal<number>(0);
  monthlyArrivals = signal<number>(0);
  monthlyDepartures = signal<number>(0);
  arrivalsCount = signal<number>(0);
  departuresCount = signal<number>(0);
  calendarDays = signal<CalendarDay[]>([]);
  private loadingCalendarSignal = signal(false);
  loadingCalendar = this.loadingCalendarSignal.asReadonly();
  monthlyArrivalsList = signal<IDashboard[]>([]);
  monthlyDeparturesList = signal<IDashboard[]>([]);
  arrivalsByDate = signal<ClientApartmentDTO[]>([]);
  departuresByDate = signal<ClientApartmentDTO[]>([]);

  filteredBookingsSignal: WritableSignal<{ content: IDashboard[]; totalElements: number }> = signal(
    {
      content: [],
      totalElements: 0,
    },
  );
  loadingFilteredSignal = signal(false);
  loadingDashboard = this.loadingFilteredSignal.asReadonly();
  filteredBookings: Signal<IDashboard[]> = computed(() => this.filteredBookingsSignal().content);
  totalElements: Signal<number> = computed(() => this.filteredBookingsSignal().totalElements);

  getDashboardData(): void {
    this.loadFilteredBookings('', 0, 1000, 'arrivalDate', 'desc', false);
  }

  loadStats(): void {
    this.loadingStatsSignal.set(true);

    this.http
      .get<DashboardStats>(`${this.apiUrl}/dashboard/stats`)
      .pipe(finalize(() => this.loadingStatsSignal.set(false)))
      .subscribe({
        next: (stats) => {
          this.totalBookings.set(stats.totalBookings);
          this.totalApartments.set(stats.totalApartments);
          this.occupancy.set(stats.occupancy);
          this.growth.set(stats.growth);
          this.todayBookings.set(stats.todayBookings);
        },
        error: (error) => console.error('❌ Error fetching dashboard stats:', error),
      });
  }

  loadFilteredBookings(
    lastName: string,
    page: number,
    size: number,
    sortBy: string,
    sortDir: string,
    onlyActive: boolean,
  ): void {
    this.loadingFilteredSignal.set(true);
    this.http
      .get<{ content: IDashboard[]; totalElements: number }>(`${this.apiUrl}/dashboard/filter`, {
        params: {
          lastName,
          page: page.toString(),
          size: size.toString(),
          sortBy,
          sortDir,
          onlyActive: onlyActive.toString(),
        },
      })
      .pipe(finalize(() => this.loadingFilteredSignal.set(false)))
      .subscribe({
        next: (data) => {
          this.filteredBookingsSignal.set({
            content: data.content,
            totalElements: data.totalElements,
          });
        },
        error: (error) => {
          console.error('❌ Error fetching filtered dashboard:', error);
          this.filteredBookingsSignal.set({ content: [], totalElements: 0 });
        },
      });
  }
  getCalendar(month: number, year: number): void {
    this.loadingCalendarSignal.set(true);
    this.http
      .get<CalendarDay[]>(`${this.apiUrl}/dashboard/calendar`, {
        params: { month: month.toString(), year: year.toString() },
      })
      .pipe(finalize(() => this.loadingCalendarSignal.set(false)))
      .subscribe({
        next: (days) => this.calendarDays.set(days),
        error: (error) => {
          console.error('❌ Error fetching calendar:', error);
          this.calendarDays.set([]);
        },
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
