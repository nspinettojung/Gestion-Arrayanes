import { Component, computed, input, output, signal } from '@angular/core';
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
  data = input<unknown[]>([]);
  pageSize = input(5);
  pageChange = output<number>();
  currentPage = signal(0);

  pagedData = computed(() => {
    const data = this.data();
    const startIndex = this.currentPage() * this.pageSize();
    return data.slice(startIndex, startIndex + this.pageSize());
  });

  totalPages = computed(() => {
    return Math.ceil(this.data().length / this.pageSize());
  });

  setPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
      this.pageChange.emit(page);
    }
  }
}
