import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { Router } from '@angular/router';
import { of } from 'rxjs';

import { Mainreservation } from './mainreservation';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';
import { NotificationService } from '../../core/services/notification/notification';
import { IBooking } from '../../core/interface/booking/booking';

const apartmentMock = {
  apartmentsData: signal([]),
  getApartments: () => undefined,
};

const savedBooking: IBooking = {
  id: 'booking-1',
  transactionCode: 'RS-2026-0007',
  arrivalDate: '2026-09-22',
  departureDate: '2026-09-23',
  nights: 1,
  clientId: 'client-1',
  apartmentId: 'apartment-1',
  amountPeople: 1,
  totalPrice: 10_000,
};

const bookingMock = {
  getBookings: () => undefined,
  getCurrentDate: () => of('2026-09-22'),
  createBookingWithClient: () => of(savedBooking),
};

const notificationMock = {
  success: vi.fn(),
  error: vi.fn(),
  info: vi.fn(),
};

const routerMock = {
  navigate: vi.fn(),
};

describe('Mainreservation', () => {
  let component: Mainreservation;
  let fixture: ComponentFixture<Mainreservation>;

  beforeEach(async () => {
    vi.clearAllMocks();

    await TestBed.configureTestingModule({
      imports: [Mainreservation],
      providers: [
        { provide: Apartment, useValue: apartmentMock },
        { provide: Booking, useValue: bookingMock },
        { provide: NotificationService, useValue: notificationMock },
        { provide: Router, useValue: routerMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainreservation);
    component = fixture.componentInstance;
    component.form = new FormGroup({
      clientName: new FormControl(''),
      clientLastName: new FormControl(''),
      clientDni: new FormControl(''),
      clientPhone: new FormControl(''),
      amountPeople: new FormControl(1),
      apartmentId: new FormControl(null),
      arrivalDate: new FormControl('2026-09-22'),
      departureDate: new FormControl('2026-09-23'),
    });
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('muestra el código de transacción al guardar la reserva', () => {
    component.form.setValue({
      clientName: 'Ana',
      clientLastName: 'Pérez',
      clientDni: '12345678',
      clientPhone: '555-1234',
      amountPeople: 1,
      apartmentId: 'apartment-1',
      arrivalDate: '2026-09-22',
      departureDate: '2026-09-23',
    });

    component.submit();

    expect(notificationMock.success).toHaveBeenCalledWith(
      'Reserva guardada con éxito. Código: RS-2026-0007',
    );
  });
});
