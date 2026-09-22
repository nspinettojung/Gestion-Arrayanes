import { Component, computed, inject, signal } from '@angular/core';
import { Table } from '../assets/table/table';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { Booking } from '../../core/services/booking/booking';

@Component({
  selector: 'app-mainhistory',
  imports: [Table, CurrencyPipe, InitialsPipe, DatePipe],
  templateUrl: './mainhistory.html',
  styleUrl: './mainhistory.scss',
})
export class Mainhistory {
  private dashboardService = inject(Dashboard);
  private bookingService = inject(Booking);
  columnsName = signal<string[]>([
    'Huésped',
    'Habitación',
    'Cantidad Personas',
    'Fecha',
    'Precio Total',
  ]);
  pageSize = 5;
  currentPage = signal(0);

  ngOnInit(): void {
    this.dashboardService.getDashboardData();
  }

  bookings = computed(() => this.dashboardService.dashboardSignal());

  pagedBookings = computed(() => {
    const data = this.bookings();
    const startIndex = this.currentPage() * this.pageSize;
    return data.slice(startIndex, startIndex + this.pageSize);
  });

  totalPages = computed(() => {
    return Math.ceil(this.bookings().length / this.pageSize);
  });

  setPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  isSelected = signal<boolean>(false);

  isRowChecked(): void {
    this.isSelected.update((prev) => !prev);
  }

  inputCheckbox(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.isSelected.set(input.checked);
  }

  idSelected = signal<Set<string>>(new Set());

  allSelected = computed(() => {
    this.bookings().length > 0 && this.idSelected().size === this.bookings().length;
  });

  alternateSelectAll(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.checked) {
      const allIds = this.bookings().map((booking) => booking.id);
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

  deleteSelectedBookings(): void {
    if (this.idSelected().size === 0) {
      alert('No hay reservas seleccionadas para eliminar.');
      return;
    }

    if (confirm('¿Estás seguro de que quieres eliminar las reservas seleccionadas?')) {
      const bookingsIds = Array.from(this.idSelected());
      this.bookingService.deleteBookingList(bookingsIds).subscribe({
        next: () => {
          alert('Reservas eliminadas correctamente.');
          this.idSelected.set(new Set());
          this.dashboardService.loadDashboard();
        },
        error: () => {
          alert('Error al eliminar las reservas. Por favor intenta nuevamente más tarde.');
        },
      });
    }
  }

  deleteBooking(bookingId: string): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta reserva?')) {
      this.bookingService.deleteBooking(bookingId).subscribe({
        next: () => {
          alert('Reserva eliminada correctamente.');
          this.dashboardService.loadDashboard();
        },
        error: () => {
          alert('Failed to delete booking. Please try again later.');
        },
      });
    }
  }
}
