import { Component, effect, HostListener, input, output } from '@angular/core';

@Component({
  selector: 'gda-modal',
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
})
export class Modal {
  title = input<string>('');
  show = input<boolean>(false);
  busy = input<boolean>(false);
  closeRequested = output<void>();

  constructor() {
    effect((onCleanup) => {
      const isOpen = this.show();
      document.body.style.overflow = isOpen ? 'hidden' : '';
      onCleanup(() => {
        document.body.style.overflow = '';
      });
    });
  }

  @HostListener('document:keydown.escape')
  onEscape(): void {
    if (this.show()) {
      this.onClose();
    }
  }

  onClose(): void {
    if (this.busy()) {
      return;
    }
    this.closeRequested.emit();
  }
}
