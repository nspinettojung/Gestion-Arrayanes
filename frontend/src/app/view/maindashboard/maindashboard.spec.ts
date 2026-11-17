import { ComponentFixture, TestBed } from '@angular/core/testing';
import { HttpErrorResponse, provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
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
    status: 'ACTIVE',
    version: 3,
  };
}

describe('Maindashboard', () => {
  let component: Maindashboard;
  let fixture: ComponentFixture<Maindashboard>;
  let dashboard: Dashboard;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Maindashboard],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Maindashboard);
    component = fixture.componentInstance;
    component.buildForm();
    dashboard = TestBed.inject(Dashboard);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('refleja la página que devuelve el backend y su total', async () => {
    const today = toLocalIsoDate();

    fixture.detectChanges();
    await flushFilter([
      buildBooking('pasada', addDays(today, -1)),
      buildBooking('se-va-hoy', today),
      buildBooking('futura', addDays(today, 5)),
    ]);
    fixture.detectChanges();

    expect(component.bookings().map((booking) => booking.id)).toEqual([
      'pasada',
      'se-va-hoy',
      'futura',
    ]);
    expect(dashboard.totalElements()).toBe(3);
  });

  it('la búsqueda pide la página 0 al backend solo con reservas activas', () => {
    const filterSpy = vi
      .spyOn(dashboard, 'loadFilteredBookings')
      .mockImplementation(() => undefined);

    component.onSearch('RS-2026-futura');

    expect(component.searchTerm()).toBe('RS-2026-futura');
    expect(component.currentPage()).toBe(0);
    expect(filterSpy).toHaveBeenCalledWith(
      'RS-2026-futura',
      0,
      component.pageSize,
      'arrivalDate',
      'desc',
      true,
    );
  });

  it('el cambio de página conserva el término de búsqueda', () => {
    const filterSpy = vi
      .spyOn(dashboard, 'loadFilteredBookings')
      .mockImplementation(() => undefined);

    component.searchTerm.set('Perez');
    component.onPageChange(2);

    expect(component.currentPage()).toBe(2);
    expect(filterSpy).toHaveBeenCalledWith(
      'Perez',
      2,
      component.pageSize,
      'arrivalDate',
      'desc',
      true,
    );
  });

  it('envía la versión de la reserva al editarla', () => {
    const bookingService = TestBed.inject(Booking);
    const booking = buildBooking('futura', addDays(toLocalIsoDate(), 5));
    const editSpy = vi.spyOn(bookingService, 'editBooking').mockReturnValue(of(booking));
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
    const refreshSpy = vi
      .spyOn(dashboard, 'loadFilteredBookings')
      .mockImplementation(() => undefined);
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

  async function flushFilter(content: IDashboard[], totalElements = content.length): Promise<void> {
    const request = httpMock.expectOne((req) => req.url.includes('/dashboard/filter'));
    request.flush({ content, totalElements });
    // httpResource resuelve el valor en una tarea asíncrona tras la respuesta.
    await new Promise<void>((resolve) => setTimeout(resolve));
  }
});
