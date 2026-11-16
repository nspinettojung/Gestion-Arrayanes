import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IDashboard } from '../../core/interface/dashboard/dashboard';
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
});
