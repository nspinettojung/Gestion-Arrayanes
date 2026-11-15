import { Component, computed, effect, input, model, output } from '@angular/core';
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
  /** Filas de la página actual (modo cliente) o de la página pedida al backend. */
  data = input<unknown[]>([]);
  pageSize = input(5);
  loading = input(false);
  emptyMessage = input('No hay datos para mostrar.');
  /** Total real de registros cuando la paginación la resuelve el backend. */
  totalElements = input<number | null>(null);

  page = model(0);
  pageChange = output<number>();

  readonly skeletonRows = [0, 1, 2, 3];

  constructor() {
    // Si se elimina el último registro de la última página, volvemos a una página válida.
    effect(() => {
      const lastPage = this.totalPages() - 1;
      if (this.page() > lastPage) {
        this.page.set(lastPage);
        this.pageChange.emit(lastPage);
      }
    });
  }

  totalItems = computed(() => this.totalElements() ?? this.data().length);

  totalPages = computed(() => {
    return Math.max(1, Math.ceil(this.totalItems() / this.pageSize()));
  });

  pages = computed(() => Array.from({ length: this.totalPages() }, (_, index) => index));

  hasData = computed(() => this.totalItems() > 0);

  shownFrom = computed(() => {
    return this.hasData() ? this.page() * this.pageSize() + 1 : 0;
  });

  shownTo = computed(() => {
    return Math.min((this.page() + 1) * this.pageSize(), this.totalItems());
  });

  setPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.page.set(page);
      this.pageChange.emit(page);
    }
  }
}
