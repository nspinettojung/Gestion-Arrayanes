import {
  Component,
  computed,
  DestroyRef,
  HostListener,
  inject,
  signal,
  OnInit,
} from '@angular/core';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';
import { CommonModule } from '@angular/common';
import {
  AbstractControl,
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NotificationService } from '../../core/services/notification/notification';
import { finalize } from 'rxjs';
import { addDays, daysBetween, toLocalIsoDate } from '../../core/utils/date';
import { calculateBookingPrice, EXTRA_PERSON_PRICE } from '../../core/utils/booking-price';
import { BookingCreateRequest } from '../../core/interface/booking/booking';
import { IApartment } from '../../core/interface/apartment/apartment';
import { ArsCurrencyPipe } from '../assets/pipes/ars-currency-pipe';

@Component({
  selector: 'app-mainreservation',
  imports: [CommonModule, ReactiveFormsModule, ArsCurrencyPipe],
  templateUrl: './mainreservation.html',
  styleUrl: './mainreservation.scss',
})
export class Mainreservation implements OnInit {
  private apartmentsService = inject(Apartment);
  private bookingsService = inject(Booking);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private destroyRef = inject(DestroyRef);
  private notifications = inject(NotificationService);

  today = signal<string>(toLocalIsoDate());
  form!: FormGroup;
  saving = signal(false);

  apartments = this.apartmentsService.apartmentsData;
  selectedApartment = signal<IApartment | null>(null);
  selectedMaxPeople = signal<number | null>(null);
  nights = signal(0);
  extraPeopleCount = signal(0);
  totalPrice = signal(0);
  extraPersonPrice = EXTRA_PERSON_PRICE;

  tomorrowDate = computed(() => addDays(this.today(), 1));

  departureMin(): string {
    const arrival = this.form?.get('arrivalDate')?.value as string | undefined;

    return arrival ? addDays(arrival, 1) : this.tomorrowDate();
  }

  ngOnInit(): void {
    this.buildForm();

    this.apartmentsService.getApartments();
    this.bookingsService.getBookings();

    this.bookingsService
      .getCurrentDate()
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (date) => {
          const previousToday = this.today();
          this.today.set(date);

          if (this.form.get('arrivalDate')?.value === previousToday) {
            this.form.patchValue({
              arrivalDate: date,
              departureDate: addDays(date, 1),
            });
          }
        },
        error: () => {
          // El interceptor global ya muestra el error; seguimos con la fecha local.
        },
      });
  }

  @HostListener('document:keydown', ['$event'])
  onGlobalKeydown(event: KeyboardEvent): void {
    if ((event.ctrlKey || event.metaKey) && event.key === 'Enter') {
      event.preventDefault();
      this.submit();
    }
  }

  private buildForm(): void {
    this.form = this.fb.group({
      clientName: ['', Validators.required],
      clientLastName: ['', Validators.required],
      clientDni: [''],
      clientPhone: [''],
      amountPeople: [1, [Validators.required, Validators.min(1)]],
      apartmentId: [null, Validators.required],
      arrivalDate: [this.today(), Validators.required],
      departureDate: [this.tomorrowDate(), Validators.required],
    });

    this.form.valueChanges.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(() => {
      this.updateSummary();
    });

    this.updateSummary();
  }

  private updateSummary(): void {
    const { apartmentId, amountPeople, arrivalDate, departureDate } = this.form.getRawValue();
    const apartment = this.apartments().find((item) => item.id === apartmentId) ?? null;
    const people = Number(amountPeople);
    const stayNights =
      arrivalDate && departureDate ? Math.max(0, daysBetween(arrivalDate, departureDate)) : 0;

    this.selectedApartment.set(apartment);
    this.selectedMaxPeople.set(apartment?.apartmentMaxNumber ?? null);
    this.nights.set(stayNights);
    this.extraPeopleCount.set(Math.max(0, (Number.isFinite(people) ? people : 0) - 1));
    this.applyMaxPeopleValidator(apartment?.apartmentMaxNumber ?? null);

    if (!apartment || stayNights < 1 || !Number.isFinite(people) || people < 1) {
      this.totalPrice.set(0);
      return;
    }

    this.totalPrice.set(calculateBookingPrice(apartment.price, people, stayNights));
  }

  private applyMaxPeopleValidator(max: number | null): void {
    const control = this.form.get('amountPeople');

    if (!control) {
      return;
    }

    const validators: ValidatorFn[] = [Validators.required, Validators.min(1)];

    if (max && max > 0) {
      validators.push(this.maxPeopleValidator(max));
    }

    control.setValidators(validators);
    control.updateValueAndValidity({ emitEvent: false });
  }

  private maxPeopleValidator(max: number): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = Number(control.value);

      return Number.isFinite(value) && value > max ? { maxPeople: { max, actual: value } } : null;
    };
  }

  submit(): void {
    if (this.saving()) {
      return;
    }

    if (this.form.invalid) {
      this.form.markAllAsTouched();
      this.notifications.error('Revisá los campos obligatorios de la reserva.');
      return;
    }

    const {
      clientName,
      clientLastName,
      clientDni,
      clientPhone,
      apartmentId,
      amountPeople,
      arrivalDate,
      departureDate,
    } = this.form.getRawValue();

    const nights = daysBetween(arrivalDate, departureDate);

    if (nights < 1) {
      this.notifications.error('La fecha de salida debe ser posterior a la fecha de llegada.');
      return;
    }

    const apartment = this.apartments().find((item) => item.id === apartmentId);

    if (apartment && Number(amountPeople) > apartment.apartmentMaxNumber) {
      this.notifications.error(
        `El departamento admite hasta ${apartment.apartmentMaxNumber} personas.`,
      );
      return;
    }

    const bookingRequest: BookingCreateRequest = {
      arrivalDate,
      departureDate,
      nights,
      amountPeople: Number(amountPeople),
      apartmentId,
      client: {
        clientName,
        clientLastName,
        clientDni,
        clientPhone,
      },
    };

    this.saving.set(true);

    this.bookingsService
      .createBookingWithClient(bookingRequest)
      .pipe(finalize(() => this.saving.set(false)))
      .subscribe({
        next: (booking) => {
          this.notifications.success(`Reserva guardada con éxito. Código: ${booking.id}`);
          this.resetForm();
          this.router.navigate(['/dashboard']);
        },
        error: () => {
          // Conservamos los datos cargados para que el usuario pueda reintentar.
        },
      });
  }

  resetForm(): void {
    this.form.reset({
      clientName: '',
      clientLastName: '',
      clientDni: '',
      clientPhone: '',
      amountPeople: 1,
      apartmentId: null,
      arrivalDate: this.today(),
      departureDate: this.tomorrowDate(),
    });
    this.updateSummary();
  }

  cancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
