import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { Mainbooking } from './mainbooking';
import { Booking } from '../../core/services/booking/booking';
import { Dashboard } from '../../core/services/dashboard/dashboard';

const bookingMock = {
  bookinglist: signal([]),
  getBookings: () => undefined,
  getCurrentDate: () => of('2026-09-22'),
};

const dashboardMock = {
  dashboardSignal: signal([]),
  monthlyArrivals: signal(0),
  monthlyDepartures: signal(0),
  monthlyArrivalsList: signal([]),
  monthlyDeparturesList: signal([]),
  arrivalsByDate: signal([]),
  departuresByDate: signal([]),
  arrivalsCount: signal(0),
  departuresCount: signal(0),
  loadDashboard: () => undefined,
  loadSummary: () => undefined,
};

describe('Mainbooking', () => {
  let component: Mainbooking;
  let fixture: ComponentFixture<Mainbooking>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainbooking],
      providers: [
        { provide: Booking, useValue: bookingMock },
        { provide: Dashboard, useValue: dashboardMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainbooking);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
