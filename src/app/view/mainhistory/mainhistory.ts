import { Component, computed, inject, signal } from '@angular/core';
import { Table } from '../assets/table/table';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { Dashboard } from '../../core/services/dashboard/dashboard';

@Component({
  selector: 'app-mainhistory',
  imports: [Table, CurrencyPipe, InitialsPipe, DatePipe],
  templateUrl: './mainhistory.html',
  styleUrl: './mainhistory.scss',
})
export class Mainhistory {
  private dashboardService = inject(Dashboard);
  columnsName = signal<string[]>([
    'Huésped',
    'Habitación',
    'Cantidad Personas',
    'Fecha',
    'Precio Total',
    'Acciones',
  ]);
  pageSize = 6;
  currentPage = signal(0);

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
}
