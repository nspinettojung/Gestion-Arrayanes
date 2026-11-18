import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import type { IDashboard } from '../../core/services/dashboard/dashboard';
import { Mainhistory } from './mainhistory';

function buildBooking(id: string, transactionCode: string | null): IDashboard {
  return {
    id,
    transactionCode,
    clientId: `client-${id}`,
    clientName: 'Ana',
    clientLastName: `Pérez ${id}`,
    apartmentId: 'apartment-1',
    apartmentName: 'AP 1',
    arrivalDate: '2026-11-10',
    departureDate: '2026-11-12',
    nights: 2,
    amountPeople: 2,
    totalPrice: 100_000,
    status: 'ACTIVE',
    version: 0,
  };
}

describe('Mainhistory', () => {
  let component: Mainhistory;
  let fixture: ComponentFixture<Mainhistory>;
  let httpMock: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainhistory],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainhistory);
    component = fixture.componentInstance;
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('permite buscar reservas por código de transacción', async () => {
    fixture.detectChanges();
    await flushFilter(
      [buildBooking('uno', 'RS-2026-0001'), buildBooking('dos', 'RS-2026-0002')],
      2,
    );

    component.searchTerm.set('RS-2026-0002');
    fixture.detectChanges();

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual(['dos']);
  });

  it('incluye reservas sin código en el listado', async () => {
    fixture.detectChanges();
    await flushFilter([buildBooking('sin-codigo', null)], 1);

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual(['sin-codigo']);
  });

  async function flushFilter(content: IDashboard[], totalElements: number): Promise<void> {
    const request = httpMock.expectOne((req) => req.url.includes('/dashboard/filter'));
    request.flush({ content, totalElements });
    // httpResource resuelve el valor en una tarea asíncrona tras la respuesta.
    await new Promise<void>((resolve) => setTimeout(resolve));
  }
});
