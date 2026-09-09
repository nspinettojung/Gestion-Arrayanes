import { Component, computed, inject, signal } from '@angular/core';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { CurrencyPipe, DatePipe } from '@angular/common';
import { Booking } from '../../core/services/booking/booking';
import { InitialsPipe } from '../assets/pipes/initials-pipe';

import { Modal } from '../assets/modal/modal';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Apartment } from '../../core/services/apartment/apartment';
import { Card } from './card/card';
import { Table } from '../assets/table/table';
import { IDashboard } from '../../core/interface/dashboard/dashboard';

@Component({
  selector: 'app-maindashboard',
  imports: [Modal, Card, CurrencyPipe, InitialsPipe, DatePipe, Table, ReactiveFormsModule],
  templateUrl: './maindashboard.html',
  styleUrl: './maindashboard.css',
})
export class Maindashboard {
  private dashboardService = inject(Dashboard);
  private bookingService = inject(Booking);
  private apartmentService = inject(Apartment);

  apartments = this.apartmentService.apartmentsData;
  apartmentId = computed(() => this.apartments().map((ap) => ap.id));
  apartmentMaxNumber = computed(() => this.apartments().map((ap) => ap.apartmentMaxNumber));
  totalApartments = computed(() => this.dashboardService.totalApartments());
  totalBookings = computed(() => this.dashboardService.totalBookings());
  occupancy = computed(() => this.dashboardService.occupancy());
  growth = computed(() => this.dashboardService.growth());
  todayBookings = computed(() => this.dashboardService.todayBookings());
  bookingEditData = computed(() => this.bookingService.bookinglist());
  bookings = computed(() => this.dashboardService.dashboardSignal());
  today = new Date();
  todaySignal = signal<string>('');
  tomorrowDate = computed(() => {
    const tomorrow = new Date(this.todaySignal());
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  });

  columnsName = signal<string[]>([
    'Huésped',
    'Habitación',
    'Cantidad Personas',
    'Fecha',
    'Precio Total',
    'Acciones',
  ]);

  bookingForm!: FormGroup;

  pageSize = 6;
  currentPage = signal(0);
  showModal: boolean = false;

  ngOnInit() {
    this.dashboardService.getTotal('bookings');
    this.dashboardService.getTotal('apartments');
    this.dashboardService.getOccupancy();
    this.dashboardService.getBookingsGrowth();
    this.dashboardService.getTodayBookings();
    this.dashboardService.loadDashboard();
    this.apartmentService.getApartments();
    this.bookings();
    this.buildForm();
    this.activeBookings();
  }

  buildForm(): void {
    this.bookingForm = new FormGroup({
      id: new FormControl(''),
      clientId: new FormControl(''),
      clientName: new FormControl({ value: '', disabled: true }),
      clientLastName: new FormControl({ value: '', disabled: true }),
      apartmentId: new FormControl(''),
      amountPeople: new FormControl(''),
      arrivalDate: new FormControl(''),
      departureDate: new FormControl(''),
      nights: new FormControl(''),
      totalPrice: new FormControl(''),
    });
  }

  isActiveBooking(departureDate: string | Date): boolean {
    let departure: Date;

    if (typeof departureDate === 'string') {
      const [year, month, day] = departureDate.split('-').map(Number);
      departure = new Date(year, month - 1, day);
    } else {
      departure = new Date(departureDate);
    }

    const today = new Date();

    departure.setHours(0, 0, 0, 0);
    today.setHours(0, 0, 0, 0);

    return departure >= today;
  }

  private parseLocalDate(date: string | Date): Date {
    if (date instanceof Date) {
      const result = new Date(date);
      result.setHours(0, 0, 0, 0);
      return result;
    }

    const [year, month, day] = date.split('-').map(Number);

    return new Date(year, month - 1, day);
  }

  pagedBookings = computed(() => {
    const data = this.bookings();
    const startIndex = this.currentPage() * this.pageSize;
    return data.slice(startIndex, startIndex + this.pageSize);
  });

  activeBookings = computed(() =>
    this.pagedBookings().filter((booking) => this.isActiveBooking(booking.departureDate)),
  );

  deleteBooking(bookingId: string): void {
    if (confirm('¿Estás seguro de que quieres eliminar esta reserva?')) {
      this.bookingService.deleteBooking(bookingId).subscribe({
        next: () => {
          alert('Reserva eliminada correctamente.');
          this.dashboardService.loadDashboard();
        },
        error: () => {
          alert('Failed to delete booking. Please try again later.');
        },
      });
    }
  }

  openEditBooking(bookingData: any): void {
    this.bookingForm.patchValue({
      id: bookingData.id,
      clientId: bookingData.clientId,
      clientName: bookingData.clientName,
      clientLastName: bookingData.clientLastName,
      apartmentId: bookingData.apartmentId,
      amountPeople: bookingData.amountPeople,
      arrivalDate: bookingData.arrivalDate,
      departureDate: bookingData.departureDate,
      nights: bookingData.nights,
      totalPrice: bookingData.totalPrice,
    });

    this.showModal = true;
  }

  onEdit(): void {
    const bookingData = this.bookingForm.getRawValue();

    this.bookingService.editBooking(bookingData.id, bookingData).subscribe({
      next: () => {
        alert('Reserva editada correctamente.');
        this.showModal = false;
        this.bookingForm.reset();
        this.dashboardService.loadDashboard();
      },
      error: (err) => {
        alert('Error al editar la reserva. Por favor intenta nuevamente más tarde.' + err);
      },
    });
  }

  totalPages = computed(() => {
    return Math.ceil(this.bookings().length / this.pageSize);
  });

  setPage(page: number) {
    if (page >= 0 && page < this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  isSelected = signal<boolean>(false);

  isRowChecked(): void {
    this.isSelected.update((prev) => !prev);
  }

  inputCheckbox(evento: Event): void {
    const input = evento.target as HTMLInputElement;
    this.isSelected.set(input.checked);
  }

  idSelected = signal<Set<string>>(new Set());

  allSelected = computed(() => {
    this.activeBookings().length > 0 && this.idSelected().size === this.activeBookings().length;
  });

  alternateSelectAll(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.checked) {
      const allIds = this.activeBookings().map((booking) => booking.id);
      this.idSelected.set(new Set(allIds));
    } else {
      this.idSelected.set(new Set());
    }
    console.log('Selected IDs:', Array.from(this.idSelected()));
  }

  alternateSelectRow(bookingId: string): void {
    this.idSelected.update((prev) => {
      const newSet = new Set(prev);
      if (newSet.has(bookingId)) {
        newSet.delete(bookingId);
      } else {
        newSet.add(bookingId);
      }
      return newSet;
    });
    console.log('Selected IDs:', Array.from(this.idSelected()));
  }

  deleteSelectedBookings(): void {
    if (this.idSelected().size === 0) {
      alert('No hay reservas seleccionadas para eliminar.');
      return;
    }

    if (confirm('¿Estás seguro de que quieres eliminar las reservas seleccionadas?')) {
      const bookingsIds = Array.from(this.idSelected());
      this.bookingService.deleteBookingList(bookingsIds).subscribe({
        next: () => {
          alert('Reservas eliminadas correctamente.');
          this.idSelected.set(new Set());
          this.dashboardService.loadDashboard();
        },
        error: () => {
          alert('Error al eliminar las reservas. Por favor intenta nuevamente más tarde.');
        },
      });
    }
  }
}
