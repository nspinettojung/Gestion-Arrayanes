import { Component, computed, DestroyRef, inject, OnInit, signal } from '@angular/core';
import { HttpErrorResponse } from '@angular/common/http';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { finalize } from 'rxjs';
import { IDashboard } from '../../core/interface/dashboard/dashboard';
import { BookingUpdateRequest } from '../../core/interface/booking/booking';
import { daysBetween, parseLocalDate, toLocalIsoDate } from '../../core/utils/date';
import { calculateBookingPrice } from '../../core/utils/booking-price';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';
import { ConfirmationService } from '../../core/services/confirmation/confirmation';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { NotificationService } from '../../core/services/notification/notification';
import { InitialsPipe } from '../assets/pipes/initials-pipe';
import { LocalDatePipe } from '../assets/pipes/local-date-pipe';
import { ArsCurrencyPipe } from '../assets/pipes/ars-currency-pipe';
import { SearchInput } from '../assets/search-input/search-input';
import { Modal } from '../assets/modal/modal';
import { Table } from '../assets/table/table';
import { Card } from './card/card';

interface EditPreview {
  nights: number;
  totalPrice: number;
  apartmentName: string;
}

@Component({
  selector: 'app-maindashboard',
  imports: [
    Modal,
    Card,
    InitialsPipe,
    LocalDatePipe,
    ArsCurrencyPipe,
    SearchInput,
    Table,
    ReactiveFormsModule,
  ],
  templateUrl: './maindashboard.html',
  styleUrl: './maindashboard.css',
})
export class Maindashboard implements OnInit {
  private dashboardService = inject(Dashboard);
  private bookingService = inject(Booking);
  private apartmentService = inject(Apartment);
  private notifications = inject(NotificationService);
  private confirmation = inject(ConfirmationService);
  private destroyRef = inject(DestroyRef);

  bookingsLoading = this.dashboardService.loadingDashboard;
  loadingStats = this.dashboardService.loadingStats;
  saving = signal(false);
  deletingId = signal<string | null>(null);
  deletingSelection = signal(false);
  editPreview = signal<EditPreview | null>(null);

  apartments = this.apartmentService.apartmentsData;
  totalApartments = this.dashboardService.totalApartments;
  totalBookings = this.dashboardService.totalBookings;
  occupancy = this.dashboardService.occupancy;
  growth = this.dashboardService.growth;
  todayBookings = this.dashboardService.todayBookings;
  bookings = this.dashboardService.dashboardSignal;

  /**
   * El dashboard muestra solo reservas activas: las que todavía no hicieron
   * check-out. Se compara en fecha local para evitar el corrimiento UTC.
   */
  private isActiveBooking(departureDate: string): boolean {
    if (!departureDate) {
      return false;
    }

    const departure = parseLocalDate(departureDate);
    const today = parseLocalDate(toLocalIsoDate());

    return departure >= today;
  }

  filteredBookings = computed(() => {
    const term = this.searchTerm().trim().toLowerCase();
    const bookings = this.bookings()
      .filter((booking) => this.isActiveBooking(booking.departureDate))
      .sort((a, b) => b.arrivalDate.localeCompare(a.arrivalDate));

    if (!term) {
      return bookings;
    }

    return bookings.filter((booking) =>
      `${booking.clientName} ${booking.clientLastName} ${booking.transactionCode ?? ''}`
        .toLowerCase()
        .includes(term),
    );
  });

  pagedBookings = computed(() => {
    const startIndex = this.currentPage() * this.pageSize;

    return this.filteredBookings().slice(startIndex, startIndex + this.pageSize);
  });

  total = computed(() => this.filteredBookings().length);

  growthLabel = computed(() => {
    const value = Math.round(this.growth() * 10) / 10;
    const sign = value >= 0 ? '+' : '';

    return `${sign}${value}% vs ayer`;
  });

  growthClass = computed(() =>
    this.growth() >= 0
      ? 'mt-4 flex items-center text-xs text-green-600 font-bold'
      : 'mt-4 flex items-center text-xs text-error font-bold',
  );

  columnsName = signal<string[]>([
    'Código',
    'Huésped',
    'Habitación',
    'Cantidad Personas',
    'Fecha',
    'Precio Total',
  ]);

  bookingForm!: FormGroup;

  pageSize = 5;
  currentPage = signal(0);
  searchTerm = signal('');
  editingTransactionCode = signal<string | null>(null);
  showModal = false;

  ngOnInit(): void {
    this.dashboardService.loadStats();
    this.dashboardService.getDashboardData();
    this.apartmentService.getApartments();
    this.buildForm();
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.currentPage.set(0);
    this.idSelected.set(new Set());
  }

  onPageChange(page: number): void {
    this.currentPage.set(page);
  }

  buildForm(): void {
    this.bookingForm = new FormGroup({
      id: new FormControl(''),
      clientId: new FormControl(''),
      clientName: new FormControl({ value: '', disabled: true }),
      clientLastName: new FormControl({ value: '', disabled: true }),
      apartmentId: new FormControl(null, Validators.required),
      amountPeople: new FormControl(1, [Validators.required, Validators.min(1)]),
      arrivalDate: new FormControl('', Validators.required),
      departureDate: new FormControl('', Validators.required),
      version: new FormControl(0),
    });

    this.bookingForm.valueChanges
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => this.updatePreview());
  }

  private updatePreview(): void {
    const { apartmentId, amountPeople, arrivalDate, departureDate } =
      this.bookingForm.getRawValue();
    const apartment = this.apartments().find((item) => item.id === apartmentId);
    const people = Number(amountPeople);

    if (!apartment || !arrivalDate || !departureDate || !Number.isFinite(people)) {
      this.editPreview.set(null);
      return;
    }

    const nights = daysBetween(arrivalDate, departureDate);

    if (nights < 1) {
      this.editPreview.set(null);
      return;
    }

    const totalPrice = calculateBookingPrice(apartment.price, people, nights);

    this.editPreview.set({ nights, totalPrice, apartmentName: apartment.apartmentName });
  }

  async deleteBooking(bookingId: string): Promise<void> {
    if (this.deletingId()) {
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      '¿Seguro que querés eliminar esta reserva? Esta acción no se puede deshacer.',
    );

    if (!confirmed) {
      return;
    }

    this.deletingId.set(bookingId);
    this.bookingService
      .deleteBooking(bookingId)
      .pipe(finalize(() => this.deletingId.set(null)))
      .subscribe({
        next: () => {
          this.notifications.success('Reserva eliminada correctamente.');
          this.dashboardService.getDashboardData();
          this.dashboardService.loadStats();
        },
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }

  openEditBooking(booking: IDashboard): void {
    this.editingTransactionCode.set(booking.transactionCode);
    this.bookingForm.patchValue({
      id: booking.id,
      clientId: booking.clientId,
      clientName: booking.clientName,
      clientLastName: booking.clientLastName,
      apartmentId: booking.apartmentId,
      amountPeople: booking.amountPeople,
      arrivalDate: booking.arrivalDate,
      departureDate: booking.departureDate,
      version: booking.version,
    });

    this.updatePreview();
    this.showModal = true;
  }

  closeModal(): void {
    if (this.saving()) {
      return;
    }

    this.showModal = false;
    this.editPreview.set(null);
    this.editingTransactionCode.set(null);
  }

  onEdit(): void {
    if (this.saving()) {
      return;
    }

    if (this.bookingForm.invalid) {
      this.bookingForm.markAllAsTouched();
      this.notifications.error('Revisá los datos de la reserva antes de guardar.');
      return;
    }

    const bookingData = this.bookingForm.getRawValue();
    const nights = daysBetween(bookingData.arrivalDate, bookingData.departureDate);

    if (nights < 1) {
      this.notifications.error('La fecha de salida debe ser posterior a la fecha de llegada.');
      return;
    }

    const payload: BookingUpdateRequest = {
      arrivalDate: bookingData.arrivalDate,
      departureDate: bookingData.departureDate,
      nights,
      clientId: bookingData.clientId,
      apartmentId: bookingData.apartmentId,
      amountPeople: Number(bookingData.amountPeople),
      version: Number(bookingData.version),
    };

    this.saving.set(true);

    this.bookingService
      .editBooking(bookingData.id, payload)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Reserva editada correctamente.');
          this.showModal = false;
          this.editPreview.set(null);
          this.editingTransactionCode.set(null);
          this.bookingForm.reset({ amountPeople: 1 });
          this.dashboardService.getDashboardData();
          this.dashboardService.loadStats();
        },
        error: (error: unknown) => {
          if (!(error instanceof HttpErrorResponse) || error.status !== 409) {
            // Conservamos los datos cargados para que el usuario pueda reintentar.
            return;
          }

          // Otra persona modificó la reserva: cerramos el modal y recargamos
          // para que la próxima edición use la versión vigente.
          this.showModal = false;
          this.editPreview.set(null);
          this.editingTransactionCode.set(null);
          this.bookingForm.reset({ amountPeople: 1, version: 0 });
          this.dashboardService.getDashboardData();
          this.dashboardService.loadStats();
        },
      });
  }

  idSelected = signal<Set<string>>(new Set());

  allSelected = computed(() => {
    const page = this.pagedBookings();

    return page.length > 0 && page.every((booking) => this.idSelected().has(booking.id));
  });

  alternateSelectAll(event: Event): void {
    const input = event.target as HTMLInputElement;

    if (input.checked) {
      const allIds = this.pagedBookings().map((booking) => booking.id);
      this.idSelected.set(new Set(allIds));
    } else {
      this.idSelected.set(new Set());
    }
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
  }

  async deleteSelectedBookings(): Promise<void> {
    if (this.deletingSelection()) {
      return;
    }

    if (this.idSelected().size === 0) {
      this.notifications.info('No hay reservas seleccionadas para eliminar.');
      return;
    }

    const confirmed = await this.confirmation.confirmDelete(
      `¿Seguro que querés eliminar ${this.idSelected().size} reserva(s)? Esta acción no se puede deshacer.`,
    );

    if (!confirmed) {
      return;
    }

    const bookingsIds = Array.from(this.idSelected());
    this.deletingSelection.set(true);

    this.bookingService
      .deleteBookingList(bookingsIds)
      .pipe(finalize(() => this.deletingSelection.set(false)))
      .subscribe({
        next: () => {
          this.notifications.success('Reservas eliminadas correctamente.');
          this.idSelected.set(new Set());
          this.dashboardService.getDashboardData();
          this.dashboardService.loadStats();
        },
        error: () => {
          // El interceptor global ya muestra el error.
        },
      });
  }
}
