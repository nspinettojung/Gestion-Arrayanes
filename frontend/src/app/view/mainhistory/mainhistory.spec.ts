import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IDashboard } from '../../core/interface/dashboard/dashboard';
import { Dashboard } from '../../core/services/dashboard/dashboard';
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
  };
}

describe('Mainhistory', () => {
  let component: Mainhistory;
  let fixture: ComponentFixture<Mainhistory>;
  let dashboard: Dashboard;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Mainhistory],
    }).compileComponents();

    fixture = TestBed.createComponent(Mainhistory);
    component = fixture.componentInstance;
    dashboard = TestBed.inject(Dashboard);
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('permite buscar reservas por código de transacción', () => {
    dashboard.dashboardSignal.set([
      buildBooking('uno', 'RS-2026-0001'),
      buildBooking('dos', 'RS-2026-0002'),
    ]);
    component.searchTerm.set('RS-2026-0002');
    fixture.detectChanges();

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual(['dos']);
  });

  it('incluye reservas sin código en el listado', () => {
    dashboard.dashboardSignal.set([buildBooking('sin-codigo', null)]);
    fixture.detectChanges();

    expect(component.filteredBookings().map((booking) => booking.id)).toEqual(['sin-codigo']);
  });
});
