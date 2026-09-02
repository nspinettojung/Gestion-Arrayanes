import { Component, computed, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'gda-table',
  imports: [CommonModule],
  templateUrl: './table.html',
  styleUrl: './table.scss',
})
export class Table {
  title = input<string>('');
  columns = input<string[]>([]);
  pageSize = 6;
  currentPage = signal(0);

  pagedBookings = computed(() => {
    // const data = this.bookings();
    // const startIndex = this.currentPage() * this.pageSize;
    // return data.slice(startIndex, startIndex + this.pageSize);
  });

  totalPages = computed(() => {
    // return Math.ceil(this.bookings().length / this.pageSize);
  });

  setPage(page: number) {
    // if (page >= 0 && page < this.totalPages()) {
    //   this.currentPage.set(page);
    // }
  }
}
