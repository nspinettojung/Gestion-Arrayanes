import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { FormControl, FormGroup } from '@angular/forms';
import { of } from 'rxjs';

import { Mainreservation } from './mainreservation';
import { Apartment } from '../../core/services/apartment/apartment';
import { Booking } from '../../core/services/booking/booking';

const apartmentMock = {
  apartmentsData: signal([]),
  getApartments: () => undefined,
};

const bookingMock = {
  getBookings: () => undefined,
  getCurrentDate: () => of('2026-09-22'),
};

describe('Mainreservation', () => {
  let component: Mainreservation;
  let fixture: ComponentFixture<Mainreservation>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainreservation],
      providers: [
        { provide: Apartment, useValue: apartmentMock },
        { provide: Booking, useValue: bookingMock },
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
});
