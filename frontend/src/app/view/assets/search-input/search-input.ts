import { Component, ElementRef, HostListener, input, model, viewChild } from '@angular/core';

@Component({
  selector: 'gda-search-input',
  template: `
    <div class="relative flex items-center">
      <span
        class="material-symbols-outlined pointer-events-none absolute left-3 text-lg text-on-surface-variant"
        aria-hidden="true"
        >search</span
      >
      <input
        #field
        type="text"
        class="w-full rounded-lg border border-outline-variant bg-surface py-2 pl-10 pr-24 text-sm"
        [value]="value()"
        [placeholder]="placeholder()"
        [attr.aria-label]="placeholder()"
        (input)="onInput($event)"
        (keydown.escape)="clear()"
      />
      <div class="absolute right-2 flex items-center gap-1">
        @if (value()) {
          <button
            type="button"
            class="rounded p-1 text-on-surface-variant transition-colors hover:bg-surface-container hover:text-primary"
            aria-label="Limpiar búsqueda"
            (click)="clear()"
          >
            <span class="material-symbols-outlined text-base">close</span>
          </button>
        } @else {
          <kbd
            class="hidden rounded border border-outline-variant bg-surface-container px-1.5 py-0.5 text-[10px] font-semibold text-on-surface-variant sm:inline-block"
            >Ctrl K</kbd
          >
        }
      </div>
    </div>
  `,
})
export class SearchInput {
  private readonly field = viewChild.required<ElementRef<HTMLInputElement>>('field');

  placeholder = input('Buscar…');
  value = model('');

  @HostListener('document:keydown', ['$event'])
  onGlobalKeydown(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key.toLowerCase() === 'k') {
      event.preventDefault();
      const input = this.field().nativeElement;
      input.focus();
      input.select();
    }
  }

  onInput(event: Event): void {
    this.value.set((event.target as HTMLInputElement).value);
  }

  clear(): void {
    this.value.set('');
    this.field().nativeElement.focus();
  }
}
