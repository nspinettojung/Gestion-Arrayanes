import { Component, computed, inject, signal, OnInit } from '@angular/core';
import { Apartment, type IApartment } from '../../core/services/apartment/apartment';
import { Table } from '../assets/table/table';

import { Modal } from '../assets/modal/modal';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { NotificationService } from '../../core/services/notification/notification';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { finalize } from 'rxjs';
import { SearchInput } from '../assets/search-input/search-input';
import { ArsCurrencyPipe } from '../assets/pipes/ars-currency-pipe';
import { formatArs } from '../../core/utils/currency';

@Component({
  selector: 'gda-mainapartment',
  imports: [Table, Modal, ReactiveFormsModule, SearchInput, ArsCurrencyPipe],
  templateUrl: './mainapartment.html',
  styleUrl: './mainapartment.scss',
})
export class Mainapartment implements OnInit {
  private apartmentService = inject(Apartment);
  private notifications = inject(NotificationService);
  private confirmation = inject(ConfirmationService);

  apatmentList = computed(() => this.apartmentService.apartmentsData());
  apartmentsLoading = this.apartmentService.loading;
  columnsName = signal<string[]>(['Nombre', 'Cantidad Máxima de Personas', 'Precio', 'Acciones']);
  editingId = signal<number | string | null>(null);
  showModal = false;
  isEditing = false;
  saving = signal(false);
  deletingId = signal<string | null>(null);
  priceSavingId = signal<string | null>(null);
  apartForm!: FormGroup;
  pageSize = 5;
  currentPage = signal(0);
  searchTerm = signal('');

  filteredApartments = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const apartments = this.apatmentList();

    if (!term) {
      return apartments;
    }

    return apartments.filter((apartment) =>
      [apartment.apartmentName, String(apartment.id)]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term)),
    );
  });

  pagedApartments = computed(() => {
    const startIndex = this.currentPage() * this.pageSize;
    return this.filteredApartments().slice(startIndex, startIndex + this.pageSize);
  });

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(0);
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

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
    if (this.priceSavingId()) {
      return;
    }

    const newPrice = Number(newPriceStr);
    if (!Number.isFinite(newPrice) || newPrice <= 0) {
      this.notifications.error('Ingresá un precio válido mayor a cero.');
      this.editingId.set(null);
      return;
    }

    this.priceSavingId.set(id);
    this.apartmentService
      .editPrice(id, newPrice)
      .pipe(finalize(() => this.priceSavingId.set(null)))
      .subscribe({
        next: (updated) => {
          this.notifications.success(`Precio actualizado a ${formatArs(updated.price)}.`);
          this.editingId.set(null);
          this.apartmentService.getApartments();
        },
        error: () => {
          // El interceptor global ya muestra el error.
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

  openCreateModal() {
    this.isEditing = false;
    this.editingId.set(null);
    this.apartForm.reset();
    this.showModal = true;
  }

  // Abrir modal en modo Edición pasándole el departamento a modificar
  openEditModal(apartment: IApartment): void {
    this.isEditing = true;
    this.editingId.set(apartment.id);
    this.apartForm.patchValue({
      apartmentName: apartment.apartmentName,
      apartmentMaxNumber: apartment.apartmentMaxNumber,
      price: apartment.price,
    });
    this.showModal = true;
  }

  // Cerrar y limpiar modal
  closeModal() {
    this.showModal = false;
    this.apartForm.reset();
    this.isEditing = false;
    this.editingId.set(null);
  }

  // Método unificado para guardar
  saveApartment() {
    if (this.saving()) {
      return;
    }

    if (this.apartForm.invalid) {
      this.apartForm.markAllAsTouched();
      this.notifications.error('Revisá los datos del departamento antes de guardar.');
      return;
    }

    const formData = this.apartForm.value;
    const editingId = this.editingId();
    const request =
      this.isEditing && editingId !== null
        ? this.apartmentService.editApartment(String(editingId), formData)
        : this.apartmentService.addApartment(formData);

    this.saving.set(true);
    request.pipe(finalize(() => this.saving.set(false))).subscribe({
      next: () => {
        this.notifications.success(
          this.isEditing
            ? 'Departamento actualizado correctamente.'
            : 'Departamento agregado correctamente.',
        );
        this.closeModal();
        this.apartmentService.getApartments();
      },
      error: () => {
        // Conservamos los datos cargados para que el usuario pueda reintentar.
      },
    });
  }

  async deleteApartment(id: string): Promise<void> {
    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      '¿Seguro que querés eliminar este departamento? Esta acción no se puede deshacer.',
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(id);
    this.apartmentService
      .deleteApartment(id)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.notifications.success('Departamento eliminado correctamente.');
          this.apartmentService.getApartments();
        },
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }
}
