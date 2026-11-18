import { computed, effect, inject, Injectable, signal, Signal } from '@angular/core';
import { environment } from '../../../../environments/environment';
import { HttpClient, httpResource } from '@angular/common/http';
import { IDashboard } from '../../interface/dashboard/dashboard';
import { DashboardStats } from '../../interface/dashboard/dashboard-stats';
import { DashboardSummary } from '../../interface/dashboard/dashboard-summary';
import { ClientApartmentDTO } from '../../interface/clients/client-apartment-dto';

interface BookingFilter {
  lastName: string;
  page: number;
  size: number;
  sortBy: string;
  sortDir: string;
  onlyActive: boolean;
}

interface SummaryParams {
  month: number;
  year: number;
  day?: number;
}

@Injectable({
  providedIn: 'root',
})
export class Dashboard {
  private apiUrl = environment.apiUrl;
  private http = inject(HttpClient);

  private bookingFilter = signal<BookingFilter | null>(null);
  private summaryParams = signal<SummaryParams | null>(null);

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
  /** Todas las cifras del mes/calendario/día llegan en una sola respuesta. */
  private summaryResource = httpResource<DashboardSummary>(() => {
    const params = this.summaryParams();

    if (!params) {
      return undefined;
    }

    return {
      url: `${this.apiUrl}/dashboard/summary`,
      params: {
        year: params.year.toString(),
        month: params.month.toString(),
        ...(params.day === undefined ? {} : { day: params.day.toString() }),
      },
    };
  });

  loadingStats = computed(() => this.statsResource.isLoading());
  totalBookings = computed(() => this.statsResource.value()?.totalBookings ?? 0);
  totalApartments = computed(() => this.statsResource.value()?.totalApartments ?? 0);
  occupancy = computed(() => this.statsResource.value()?.occupancy ?? 0);
  growth = computed(() => this.statsResource.value()?.growth ?? 0);
  todayBookings = computed(() => this.statsResource.value()?.todayBookings ?? 0);

  monthlyArrivals = computed(() => this.summaryResource.value()?.monthly.arrivalsCount ?? 0);
  monthlyDepartures = computed(() => this.summaryResource.value()?.monthly.departuresCount ?? 0);
  arrivalsCount = computed(() => this.summaryResource.value()?.day.arrivalsCount ?? 0);
  departuresCount = computed(() => this.summaryResource.value()?.day.departuresCount ?? 0);
  monthlyArrivalsList = computed<ClientApartmentDTO[]>(
    () => this.summaryResource.value()?.monthly.arrivals ?? [],
  );
  monthlyDeparturesList = computed<ClientApartmentDTO[]>(
    () => this.summaryResource.value()?.monthly.departures ?? [],
  );
  arrivalsByDate = computed<ClientApartmentDTO[]>(
    () => this.summaryResource.value()?.day.arrivals ?? [],
  );
  departuresByDate = computed<ClientApartmentDTO[]>(
    () => this.summaryResource.value()?.day.departures ?? [],
  );

  loadingFilteredSignal = computed(() => this.filteredResource.isLoading());
  filteredBookings: Signal<IDashboard[]> = computed(
    () => this.filteredResource.value()?.content ?? [],
  );
  totalElements: Signal<number> = computed(() => this.filteredResource.value()?.totalElements ?? 0);

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
      const error = this.summaryResource.error();
      if (error) {
        console.error('❌ Error fetching dashboard summary:', error);
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

  /** Carga mes (y opcionalmente día) con una única petición al backend. */
  loadSummary(month: number, year: number, day?: number): void {
    const current = this.summaryParams();

    if (
      current !== null &&
      current.month === month &&
      current.year === year &&
      current.day === day
    ) {
      this.summaryResource.reload();
      return;
    }

    this.summaryParams.set({ month, year, day });
  }
}
