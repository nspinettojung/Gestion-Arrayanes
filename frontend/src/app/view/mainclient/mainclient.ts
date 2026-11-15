import { Component, computed, inject, signal } from '@angular/core';
import { Clients } from '../../core/services/clients/clients';
import { CommonModule } from '@angular/common';
import { Modal } from '../assets/modal/modal';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { Table } from '../assets/table/table';
import { IClients } from '../../core/interface/clients/clients';
import { NotificationService } from '../../core/services/notification/notification';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { finalize } from 'rxjs';
import { SearchInput } from '../assets/search-input/search-input';
import { LocalDatePipe } from '../assets/pipes/local-date-pipe';

@Component({
  selector: 'gda-mainclient',
  imports: [
    CommonModule,
    Modal,
    InitialsPipe,
    LocalDatePipe,
    Table,
    ReactiveFormsModule,
    SearchInput,
  ],
  templateUrl: './mainclient.html',
  styleUrl: './mainclient.scss',
})
export class Mainclient {
  private clientService = inject(Clients);
  private notifications = inject(NotificationService);
  private confirmation = inject(ConfirmationService);

  showModal: boolean = false;
  saving = signal(false);
  deletingId = signal<string | null>(null);
  columnsName = signal<string[]>(['Nombre del Cliente', 'Contacto', 'Última Estancia', 'Acciones']);

  clientslist = computed(() => this.clientService.clientslist());
  clientsLoading = this.clientService.loading;
  clientForm!: FormGroup;
  pageSize = 5;
  currentPage = signal(0);
  searchTerm = signal('');

  filteredClients = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const clients = this.clientslist();

    if (!term) {
      return clients;
    }

    return clients.filter((client) =>
      [client.clientName, client.clientLastName, client.clientDni, client.clientPhone]
        .filter(Boolean)
        .some((value) => value.toLowerCase().includes(term)),
    );
  });

  pagedClients = computed(() => {
    const data = this.filteredClients();
    const startIndex = this.currentPage() * this.pageSize;
    return data.slice(startIndex, startIndex + this.pageSize);
  });

  ngOnInit() {
    this.clientService.getClients();
    this.buildForm();
  }

  buildForm(): void {
    this.clientForm = new FormGroup({
      id: new FormControl(''),
      clientName: new FormControl(''),
      clientLastName: new FormControl(''),
      clientDni: new FormControl(''),
      clientPhone: new FormControl(''),
      lastCheckIn: new FormControl(''),
    });
  }

  async deleteClient(clientId: string): Promise<void> {
    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      '¿Seguro que querés eliminar este cliente? Esta acción no se puede deshacer.',
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(clientId);
    this.clientService
      .deleteClient(clientId)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => this.notifications.success('Cliente eliminado correctamente.'),
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }

  openEditClient(client: IClients): void {
    this.clientForm.patchValue({
      id: client.id,
      clientName: client.clientName,
      clientLastName: client.clientLastName,
      clientDni: client.clientDni,
      clientPhone: client.clientPhone,
      lastCheckIn: client.lastCheckIn,
    });

    this.showModal = true;
  }

  onEdit(): void {
    if (this.saving()) {
      return;
    }

    if (this.clientForm.invalid) {
      this.clientForm.markAllAsTouched();
      this.notifications.error('Revisá los datos del cliente antes de guardar.');
      return;
    }

    const client = this.clientForm.getRawValue();
    this.saving.set(true);

    this.clientService
      .editClient(client.id, client)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Cliente actualizado correctamente.');
          this.resetForm();
          this.showModal = false;
        },
        error: () => {
          // Conservamos los datos cargados para que el usuario pueda reintentar.
        },
      });
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(0);
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  resetForm(): void {
    this.clientForm = new FormGroup({
      id: new FormControl(''),
      clientName: new FormControl(''),
      clientLastName: new FormControl(''),
      clientDni: new FormControl(''),
      clientPhone: new FormControl(''),
      lastCheckIn: new FormControl(''),
    });
  }
}
