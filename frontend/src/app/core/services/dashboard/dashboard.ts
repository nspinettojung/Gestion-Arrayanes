import { inject, Injectable, signal, computed, Signal, effect } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, httpResource } from '@angular/common/http';
import { IDashboard } from '../../interface/dashboard/dashboard';
import { DashboardStats } from '../../interface/dashboard/dashboard-stats';
import { CalendarDay } from '../../interface/dashboard/calendar-day';
import { ClientApartmentDTO } from '../../interface/clients/client-apartment-dto';
import { Observable } from 'rxjs';

interface BookingFilter {
  lastName: string;
  page: number;
  size: number;
  sortBy: string;
  sortDir: string;
  onlyActive: boolean;
}

@Injectable({
  providedIn: 'root',
})
export class Dashboard {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  private bookingFilter = signal<BookingFilter | null>(null);
  private calendarParams = signal<{ month: number; year: number } | null>(null);

  private statsResource = httpResource<DashboardStats>(() => `${this.apiUrl}/dashboard/stats`);
  private filteredResource = httpResource<{ content: IDashboard[]; totalElements: number }>(() => {
    const filter = this.bookingFilter();

    if (!filter) {
      return undefined;
    }

    return {
      url: `${this.apiUrl}/dashboard/filter`,
      params: {
        lastName: filter.lastName,
        page: filter.page.toString(),
        size: filter.size.toString(),
        sortBy: filter.sortBy,
        sortDir: filter.sortDir,
        onlyActive: filter.onlyActive.toString(),
      },
    };
  });
  private calendarResource = httpResource<CalendarDay[]>(() => {
    const params = this.calendarParams();

    if (!params) {
      return undefined;
    }

    return {
      url: `${this.apiUrl}/dashboard/calendar`,
      params: {
        month: params.month.toString(),
        year: params.year.toString(),
      },
    };
  });

  loadingStats = computed(() => this.statsResource.isLoading());
  totalBookings = computed(() => this.statsResource.value()?.totalBookings ?? 0);
  totalApartments = computed(() => this.statsResource.value()?.totalApartments ?? 0);
  occupancy = computed(() => this.statsResource.value()?.occupancy ?? 0);
  growth = computed(() => this.statsResource.value()?.growth ?? 0);
  todayBookings = computed(() => this.statsResource.value()?.todayBookings ?? 0);

  monthlyArrivals = signal<number>(0);
  monthlyDepartures = signal<number>(0);
  arrivalsCount = signal<number>(0);
  departuresCount = signal<number>(0);
  monthlyArrivalsList = signal<IDashboard[]>([]);
  monthlyDeparturesList = signal<IDashboard[]>([]);
  arrivalsByDate = signal<ClientApartmentDTO[]>([]);
  departuresByDate = signal<ClientApartmentDTO[]>([]);

  loadingFilteredSignal = computed(() => this.filteredResource.isLoading());
  filteredBookings: Signal<IDashboard[]> = computed(
    () => this.filteredResource.value()?.content ?? [],
  );
  totalElements: Signal<number> = computed(() => this.filteredResource.value()?.totalElements ?? 0);

  calendarDays = computed(() => this.calendarResource.value() ?? []);
  loadingCalendar = computed(() => this.calendarResource.isLoading());

  constructor() {
    effect(() => {
      const error = this.statsResource.error();
      if (error) {
        console.error('❌ Error fetching dashboard stats:', error);
      }
    });
    effect(() => {
      const error = this.filteredResource.error();
      if (error) {
        console.error('❌ Error fetching filtered dashboard:', error);
      }
    });
    effect(() => {
      const error = this.calendarResource.error();
      if (error) {
        console.error('❌ Error fetching calendar:', error);
      }
    });
  }

  getDashboardData(): void {
    this.loadFilteredBookings('', 0, 1000, 'arrivalDate', 'desc', false);
  }

  loadStats(): void {
    this.statsResource.reload();
  }

  /** Setea los parámetros del listado de reservas; el recurso refetcha solo. */
  loadFilteredBookings(
    lastName: string,
    page: number,
    size: number,
    sortBy: string,
    sortDir: string,
    onlyActive: boolean,
  ): void {
    const next = { lastName, page, size, sortBy, sortDir, onlyActive };
    const current = this.bookingFilter();

    if (
      current !== null &&
      current.lastName === next.lastName &&
      current.page === next.page &&
      current.size === next.size &&
      current.sortBy === next.sortBy &&
      current.sortDir === next.sortDir &&
      current.onlyActive === next.onlyActive
    ) {
      // Mismos parámetros: forzar refetch (p. ej. tras un CRUD).
      this.filteredResource.reload();
      return;
    }

    this.bookingFilter.set(next);
  }

  /** Setea el mes visible del calendario; el recurso refetcha solo. */
  getCalendar(month: number, year: number): void {
    const current = this.calendarParams();

    if (current !== null && current.month === month && current.year === year) {
      this.calendarResource.reload();
      return;
    }

    this.calendarParams.set({ month, year });
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
