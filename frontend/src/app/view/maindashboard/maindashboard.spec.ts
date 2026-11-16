import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse } from '@angular/common/http';
import { of, throwError } from 'rxjs';

import { IDashboard } from '../../core/interface/dashboard/dashboard';
import { Booking } from '../../core/services/booking/booking';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { addDays, toLocalIsoDate } from '../../core/utils/date';
import { Maindashboard } from './maindashboard';

function buildBooking(id: string, departureDate: string): IDashboard {
  return {
    id,
    transactionCode: `RS-2026-${id}`,
    clientId: `client-${id}`,
    clientName: 'Ana',
    clientLastName: `Pérez ${id}`,
    apartmentId: 'apartment-1',
    apartmentName: 'AP 1',
    arrivalDate: addDays(departureDate, -2),
    departureDate,
    nights: 2,
    amountPeople: 2,
    totalPrice: 100_000,
    version: 3,
  };
}

describe('Maindashboard', () => {
  let component: Maindashboard;
  let fixture: ComponentFixture<Maindashboard>;
  let dashboard: Dashboard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Maindashboard],
    }).compileComponents();

    fixture = TestBed.createComponent(Maindashboard);
    component = fixture.componentInstance;
    dashboard = TestBed.inject(Dashboard);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('solo muestra reservas cuyo check-out todavía no pasó', () => {
    const today = toLocalIsoDate();

    dashboard.dashboardSignal.set([
      buildBooking('pasada', addDays(today, -1)),
      buildBooking('se-va-hoy', today),
      buildBooking('futura', addDays(today, 5)),
    ]);
    fixture.detectChanges();

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual([
      'futura',
      'se-va-hoy',
    ]);
    expect(component.total()).toBe(2);
  });

  it('permite buscar reservas por código de transacción', () => {
    const today = toLocalIsoDate();

    dashboard.dashboardSignal.set([
      buildBooking('futura', addDays(today, 5)),
      buildBooking('otra', addDays(today, 6)),
    ]);
    component.searchTerm.set('RS-2026-futura');
    fixture.detectChanges();

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual(['futura']);
  });

  it('envía la versión de la reserva al editarla', () => {
    const bookingService = TestBed.inject(Booking);
    const booking = buildBooking('futura', addDays(toLocalIsoDate(), 5));
    const editSpy = vi.spyOn(bookingService, 'editBooking').mockReturnValue(of(booking));
    vi.spyOn(dashboard, 'getDashboardData').mockImplementation(() => undefined);
    vi.spyOn(dashboard, 'loadStats').mockImplementation(() => undefined);

    component.openEditBooking(booking);
    component.onEdit();

    expect(editSpy).toHaveBeenCalledWith(
      booking.id,
      expect.objectContaining({
        clientId: booking.clientId,
        apartmentId: booking.apartmentId,
        version: 3,
      }),
    );
  });

  it('cierra el modal y recarga si la reserva fue modificada por otro usuario', () => {
    const bookingService = TestBed.inject(Booking);
    const refreshSpy = vi.spyOn(dashboard, 'getDashboardData').mockImplementation(() => undefined);
    const statsSpy = vi.spyOn(dashboard, 'loadStats').mockImplementation(() => undefined);
    vi.spyOn(bookingService, 'editBooking').mockReturnValue(
      throwError(() => new HttpErrorResponse({ status: 409 })),
    );

    component.openEditBooking(buildBooking('futura', addDays(toLocalIsoDate(), 5)));
    component.onEdit();

    expect(component.showModal).toBe(false);
    expect(refreshSpy).toHaveBeenCalled();
    expect(statsSpy).toHaveBeenCalled();
  });
});
