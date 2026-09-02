import { Component, computed, inject, signal } from '@angular/core';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';
import { CommonModule } from '@angular/common';
import { FormGroup, FormBuilder, Validators, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';

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
    this.bookingsService.getCurrentDate().subscribe({
      next: (date) => this.today.set(date),
      error: (error) => alert('❌ Error fetching current date: ' + error.message),
    });

    this.form = this.fb.group({
      clientName: ['', Validators.required],
      clientLastName: ['', Validators.required],
      clientDni: ['', Validators.required],
      clientPhone: ['', Validators.required],
      amountPeople: [1, Validators.required],
      apartmentId: [null, ''],
      arrivalDate: [new Date().toISOString().split('T')[0], Validators.required],
      departureDate: [this.tomorrowDate(), Validators.required],
    });
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
        nights,
      } = this.form.value;

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
          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          if (err.status === 409) {
            alert(`❌ ${err.error.message}`);
            return;
          }

          alert(`❌ Error ${err.status}: ${err.error?.message ?? err.message}`);
        },
      });
    } else {
      alert('❌ Formulario inválido');
    }
  }

  resetForm(): void {
    this.form = this.fb.group({
      clientName: ['', Validators.required],
      clientLastName: ['', Validators.required],
      clientDni: ['', Validators.required],
      clientPhone: ['', Validators.required],
      amountPeople: [1, Validators.required],
      apartmentId: [null, ''],
      arrivalDate: [new Date().toISOString().split('T')[0], Validators.required],
      departureDate: [this.tomorrowDate(), Validators.required],
    });
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
