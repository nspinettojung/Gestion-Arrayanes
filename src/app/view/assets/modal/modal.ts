import { Component, EventEmitter, inject, input, output, Output } from '@angular/core';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';

@Component({
  selector: 'gda-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './modal.html',
  styleUrl: './modal.scss',
})
export class Modal {
  title = input<string>('');
  show = input<boolean>(false);
  close = output<void>();
  

  onClose() {
    this.close.emit();
  }
 
}
