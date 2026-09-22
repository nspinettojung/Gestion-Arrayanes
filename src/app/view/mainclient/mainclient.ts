import { Component, computed, inject, signal } from '@angular/core';
import { Clients } from '../../core/services/clients/clients';
import { Booking } from '../../core/services/booking/booking';
import { CommonModule } from '@angular/common';
import { Modal } from '../assets/modal/modal';
import { FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { Table } from '../assets/table/table';
import { IClients } from '../../core/interface/clients/clients';

@Component({
  selector: 'gda-mainclient',
  imports: [CommonModule, Modal, InitialsPipe, Table, ReactiveFormsModule],
  templateUrl: './mainclient.html',
  styleUrl: './mainclient.scss',
})
export class Mainclient {
  private clientService = inject(Clients);
  private bookingService = inject(Booking);

  showModal: boolean = false;
  columnsName = signal<string[]>(['Nombre del Cliente', 'Contacto', 'Última Estancia', 'Acciones']);

  clientslist = computed(() => this.clientService.clientslist());
  bookingCheckIn = this.bookingService.bookinglist;
  clientForm!: FormGroup;
  pageSize = 5;
  currentPage = signal(0);

  totalPages = computed(() => {
    return Math.ceil(this.clientslist().length / this.pageSize);
  });

  pagedClients = computed(() => {
    const data = this.clientslist();
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

  deleteClient(clientId: string): void {
    if (confirm('¿Estás seguro de que quieres eliminar este cliente?')) {
      this.clientService.deleteClient(clientId).subscribe({
        next: () => {
          alert('Cliente eliminado correctamente.');
        },
        error: (error) => {
          // console.error('Error eliminando cliente :', error);
          alert('Failed to delete client. Please try again later.');
        },
      });
    }
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
    const client = this.clientForm.getRawValue();

    this.clientService.editClient(client.id, client).subscribe({
      next: () => {
        alert('✅ Cliente actualizado');
        this.resetForm();
        this.showModal = false;
      },
      error: (err) => alert('❌ Error: ' + err.message),
    });
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

  getInitials(clientName: string, clientLastName: string): string {
    const firstInitial = clientName ? clientName.charAt(0).toUpperCase() : '';
    const lastInitial = clientLastName ? clientLastName.charAt(0).toUpperCase() : '';
    return `${firstInitial}${lastInitial}`;
  }

  setPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  prevPage() {
    if (this.currentPage() > 0) {
      this.currentPage.set(this.currentPage() - 1);
    }
  }

  nextPage() {
    if ((this.currentPage() + 1) * this.pageSize < this.clientslist().length) {
      this.currentPage.set(this.currentPage() + 1);
    }
  }
}
