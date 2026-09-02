import { Component, computed, inject, signal } from '@angular/core';
import { Apartment } from '../../core/services/apartment/apartment';
import { Table } from '../assets/table/table';

import { Modal } from '../assets/modal/modal';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { IApartment } from '../../core/interface/apartment/apartment';

@Component({
  selector: 'gda-mainapartment',
  imports: [Table, Modal, ReactiveFormsModule],
  templateUrl: './mainapartment.html',
  styleUrl: './mainapartment.scss',
})
export class Mainapartment {
  private apartmentService = inject(Apartment);
  apatmentList = computed(() => this.apartmentService.apartmentsData());
  columnsName = signal<string[]>(['Nombre', 'Cantidad Máxima de Personas', 'Precio', 'Acciones']);
  editingId = signal<string | null>(null);
  showModal: boolean = false;
  apartForm!: FormGroup;

  ngOnInit(): void {
    this.apartmentService.getApartments();
    this.buildForm();
  }

  startEdit(id: string): void {
    this.editingId.set(id);
  }

  cancelEdit(): void {
    this.editingId.set(null);
  }

  savePrice(id: string, newPriceStr: string): void {
    const newPrice = Number(newPriceStr);
    if (isNaN(newPrice) || newPrice <= 0) {
      alert('❌ Precio inválido');
      this.editingId.set(null);
      return;
    }

    this.apartmentService.editPrice(id, newPrice).subscribe({
      next: (updated) => {
        alert(`✅ Precio actualizado a $${updated.price}`);
        this.editingId.set(null); // 👈 cerrar modo edición
        this.apartmentService.getApartments();
      },
      error: (err) => {
        alert('❌ Error actualizando precio: ' + err.message);
        this.editingId.set(null);
      },
    });
  }

  buildForm(): void {
    this.apartForm = new FormGroup({
      id: new FormControl(''),
      apartmentName: new FormControl(''),
      apartmentMaxNumber: new FormControl(''),
      price: new FormControl(''),
    });
  }

  openAddApart() {
    this.showModal = true;
  }

  createApartment(): void {
    if (this.apartForm.valid) {
      const { id, apartmentName, apartmentMaxNumber, price } = this.apartForm.value;

      const apartRequest: IApartment = {
        id,
        apartmentName,
        apartmentMaxNumber,
        price,
      };

      this.apartmentService.addApartment(apartRequest).subscribe({
        next: () => {
          alert('El Departamento se agrego correctamente.');
          this.showModal = false;
          this.apartForm.reset();
          this.apartmentService.getApartments();
        },
        error: () => {
          alert('Error al agregar un departamento');
        },
      });
    }
  }

  deleteApartment(id: string): void {
    if (confirm('¿Estás seguro de eliminar el departamento?')) {
      this.apartmentService.deleteApartment(id).subscribe({
        next: () => {
          alert('Departamento eliminado correctamente.');
          this.apartmentService.getApartments();
        },
        error: () => {
          alert('Error al eliminar el departamento.');
        },
      });
    }
  }
}
