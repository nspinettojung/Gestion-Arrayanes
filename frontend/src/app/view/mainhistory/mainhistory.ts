import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { Table } from '../assets/table/table';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { Booking } from '../../core/services/booking/booking';
import { NotificationService } from '../../core/services/notification/notification';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { finalize } from 'rxjs';
import { SearchInput } from '../assets/search-input/search-input';
import { ArsCurrencyPipe } from '../assets/pipes/ars-currency-pipe';
import { LocalDatePipe } from '../assets/pipes/local-date-pipe';

@Component({
  selector: 'app-mainhistory',
  imports: [Table, InitialsPipe, SearchInput, ArsCurrencyPipe, LocalDatePipe],
  templateUrl: './mainhistory.html',
  styleUrl: './mainhistory.scss',
})
export class Mainhistory implements OnInit {
  private dashboardService = inject(Dashboard);
  private bookingService = inject(Booking);
  private notifications = inject(NotificationService);
  private confirmation = inject(ConfirmationService);

  columnsName = signal<string[]>([
    'Huésped',
    'Habitación',
    'Cantidad Personas',
    'Fecha',
    'Precio Total',
  ]);
  pageSize = 5;
  currentPage = signal(0);
  searchTerm = signal('');
  loading = this.dashboardService.loadingDashboard;
  deletingId = signal<string | null>(null);
  deletingSelection = signal(false);

  ngOnInit(): void {
    this.dashboardService.getDashboardData();
  }

  bookings = computed(() => this.dashboardService.dashboardSignal());

  filteredBookings = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const bookings = [...this.bookings()].sort((a, b) =>
      b.arrivalDate.localeCompare(a.arrivalDate),
    );

    if (!term) {
      return bookings;
    }

    return bookings.filter((booking) =>
      `${booking.clientName} ${booking.clientLastName}`.toLowerCase().includes(term),
    );
  });

  pagedBookings = computed(() => {
    const startIndex = this.currentPage() * this.pageSize;

    return this.filteredBookings().slice(startIndex, startIndex + this.pageSize);
  });

  total = computed(() => this.filteredBookings().length);

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(0);
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  idSelected = signal<Set<string>>(new Set());

  allSelected = computed(() => {
    const page = this.pagedBookings();

    return page.length > 0 && page.every((booking) => this.idSelected().has(booking.id));
  });

  alternateSelectAll(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.checked) {
      const allIds = this.pagedBookings().map((booking) => booking.id);
      this.idSelected.set(new Set(allIds));
    } else {
      this.idSelected.set(new Set());
    }
  }

  alternateSelectRow(bookingId: string): void {
    this.idSelected.update((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(bookingId)) {
        newSet.delete(bookingId);
      } else {
        newSet.add(bookingId);
      }
      return newSet;
    });
  }

  async deleteSelectedBookings(): Promise<void> {
    if (this.deletingSelection()) {
      return;
    }

    if (this.idSelected().size === 0) {
      this.notifications.info('No hay reservas seleccionadas para eliminar.');
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      `¿Seguro que querés eliminar ${this.idSelected().size} reserva(s)? Esta acción no se puede deshacer.`,
    );

    if (!confirmed) {
      return;
    }

    const bookingsIds = Array.from(this.idSelected());
    this.deletingSelection.set(true);

    this.bookingService
      .deleteBookingList(bookingsIds)
      .pipe(finalize(() => this.deletingSelection.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Reservas eliminadas correctamente.');
          this.idSelected.set(new Set());
          this.dashboardService.getDashboardData();
        },
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }

  async deleteBooking(bookingId: string): Promise<void> {
    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      '¿Seguro que querés eliminar esta reserva? Esta acción no se puede deshacer.',
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(bookingId);
    this.bookingService
      .deleteBooking(bookingId)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.notifications.success('Reserva eliminada correctamente.');
          this.dashboardService.getDashboardData();
        },
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }
}
