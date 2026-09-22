import { ChangeDetectorRef, Component, computed, DestroyRef, inject, signal } from '@angular/core';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';
import { CommonModule } from '@angular/common';
import { FormGroup, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-mainreservation',
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './mainreservation.html',
  styleUrl: './mainreservation.scss',
})
export class Mainreservation {
  private apartmentsService = inject(Apartment);
  private bookingsService = inject(Booking);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);
  private cdr = inject(ChangeDetectorRef);
  today = signal<string>('');
  form!: FormGroup;

  apartments = this.apartmentsService.apartmentsData;
  apartmentId = computed(() => this.apartments().map((ap) => ap.id));
  apartmentMaxNumber = computed(() => this.apartments().map((ap) => ap.apartmentMaxNumber));
  tomorrowDate = computed(() => {
    const tomorrow = new Date(this.today());
    tomorrow.setDate(tomorrow.getDate() + 1);
    return tomorrow.toISOString().split('T')[0];
  });
  selectedMaxPeople = signal<number | null>(null);

  ngOnInit(): void {
    const now = new Date();

    this.today.set(now.toISOString().split('T')[0]);

    this.apartmentsService.getApartments();
    this.bookingsService.getBookings();

    this.bookingsService
      .getCurrentDate()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (date) => {
          this.today.set(date);
          this.cdr.detectChanges();
        },
        error: (error) => alert('❌ Error fetching current date: ' + error.message),
      });

    this.form = this.fb.group({
      clientName: ['', Validators.required],
      clientLastName: ['', Validators.required],
      clientDni: [''],
      clientPhone: [''],
      amountPeople: [1, Validators.required],
      apartmentId: [null, ''],
      arrivalDate: [new Date().toISOString().split('T')[0], Validators.required],
      departureDate: [this.tomorrowDate(), Validators.required],
    });

    setTimeout(() => {
      // 1. Llamada a la API segura expuesta por tu preload.js
      if ((window as any).electron) {
        (window as any).electron.restaurarFoco();
      }

      // 2. Fallback nativo mejorado con .select() para asegurar la activación en Chromium
      const primerInput = document.querySelector(
        'input[type="number"], input[type="text"]',
      ) as HTMLInputElement;
      if (primerInput) {
        primerInput.blur();
        setTimeout(() => {
          primerInput.focus();
          primerInput.select(); // Selecciona el texto para forzar el enganche del teclado
        }, 50);
      }

      // 3. Forzar sincronización visual de Angular
      this.cdr.detectChanges();
    }, 200);
  }

  ngOnDestroy(): void {
    console.log('BOOKING FORM DESTRUIDO');
  }

  onApartmentChange(event: Event): void {
    const selectedId = (event.target as HTMLSelectElement).value;
    const apartment = this.apartments().find((ap) => ap.id === selectedId);

    this.selectedMaxPeople.set(apartment ? apartment.apartmentMaxNumber : null);
  }

  submit(): void {
    if (this.form.valid) {
      const {
        clientName,
        clientLastName,
        clientDni,
        clientPhone,
        apartmentId,
        amountPeople,
        arrivalDate,
        departureDate,
      } = this.form.value;

      const nights = this.calculateNights(arrivalDate, departureDate);
      if (nights < 1) {
        alert('❌ La fecha de salida debe ser posterior a la fecha de llegada');
        return;
      }

      const bookingRequest = {
        arrivalDate,
        departureDate,
        nights,
        amountPeople,
        apartmentId,
        client: {
          clientName,
          clientLastName,
          clientDni,
          clientPhone,
        },
      };

      this.bookingsService.createBookingWithClient(bookingRequest).subscribe({
        next: (bookingRequest) => {
          alert(`Reserva guardada con éxito. Código: ${bookingRequest.id}`);
          this.resetForm();
          setTimeout(() => {
            this.router.navigate(['/dashboard']);
          }, 50);
        },
        error: (err) => {
          if (err.status === 409) {
            alert(`❌ ${err.error.message}`);
            this.resetForm();
            return;
          }

          alert(`❌ Error ${err.status}: ${err.error?.message ?? err.message}`);
          this.resetForm();
        },
      });
    } else {
      alert('❌ Formulario inválido');
    }
  }

  private calculateNights(arrivalDate: string, departureDate: string): number {
    const arrival = new Date(`${arrivalDate}T00:00:00`);
    const departure = new Date(`${departureDate}T00:00:00`);
    const millisecondsPerDay = 24 * 60 * 60 * 1000;

    return Math.round((departure.getTime() - arrival.getTime()) / millisecondsPerDay);
  }

  resetForm(): void {
    this.form.reset({
      clientName: '',
      clientLastName: '',
      clientDni: '',
      clientPhone: '',
      amountPeople: 1,
      apartmentId: null,
      arrivalDate: new Date().toISOString().split('T')[0],
      departureDate: this.tomorrowDate(),
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
