import { ComponentFixture, TestBed } from '@angular/core/testing';
import { signal } from '@angular/core';
import { of } from 'rxjs';

import { Calendar } from './calendar';
import { Booking } from '../../../core/services/booking/booking';
import { Dashboard } from '../../../core/services/dashboard/dashboard';

const dashboardMock = {
  arrivalsCount: signal(0),
  departuresCount: signal(0),
  getMonthlyArrivalsList: () => of([]),
  getMonthlyDeparturesList: () => of([]),
};

describe('Calendar', () => {
  let component: Calendar;
  let fixture: ComponentFixture<Calendar>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Calendar],
      providers: [
        { provide: Booking, useValue: {} },
        { provide: Dashboard, useValue: dashboardMock },
      ],
    }).compileComponents();

    fixture = TestBed.createComponent(Calendar);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
