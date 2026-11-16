import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CalendarDay } from '../../core/interface/dashboard/calendar-day';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { toLocalIsoDate } from '../../core/utils/date';
import { Maincalendar } from './maincalendar';

describe('Maincalendar', () => {
  let component: Maincalendar;
  let fixture: ComponentFixture<Maincalendar>;
  let dashboard: Dashboard;
  let getCalendarSpy: ReturnType<typeof vi.spyOn>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Maincalendar],
    }).compileComponents();

    dashboard = TestBed.inject(Dashboard);
    getCalendarSpy = vi.spyOn(dashboard, 'getCalendar').mockImplementation(() => undefined);

    fixture = TestBed.createComponent(Maincalendar);
    component = fixture.componentInstance;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('pide el mes actual al backend al iniciar', () => {
    fixture.detectChanges();

    const now = new Date();
    expect(getCalendarSpy).toHaveBeenCalledWith(now.getMonth() + 1, now.getFullYear());
  });

  it('genera siempre una grilla de 42 días', () => {
    fixture.detectChanges();

    expect(component.cells()).toHaveLength(42);
  });

  it('marca como ocupados solo los días con reserva', () => {
    const today = new Date();
    const occupiedIso = toLocalIsoDate(new Date(today.getFullYear(), today.getMonth(), 15));
    const freeIso = toLocalIsoDate(new Date(today.getFullYear(), today.getMonth(), 16));
    const days: CalendarDay[] = [
      { date: occupiedIso, hasBooking: true },
      { date: freeIso, hasBooking: false },
    ];

    dashboard.calendarDays.set(days);
    fixture.detectChanges();

    const cells = component.cells();
    expect(cells.find((cell) => cell.date === occupiedIso)?.hasBooking).toBe(true);
    expect(cells.find((cell) => cell.date === freeIso)?.hasBooking).toBe(false);
  });

  it('retroceder desde enero va a diciembre del año anterior', () => {
    component.viewMonth.set(0);
    component.viewYear.set(2026);
    getCalendarSpy.mockClear();

    component.previousMonth();

    expect(component.viewMonth()).toBe(11);
    expect(component.viewYear()).toBe(2025);
    expect(getCalendarSpy).toHaveBeenCalledWith(12, 2025);
  });

  it('la navegación a enero del año siguiente desde diciembre al avanzar', () => {
    component.viewMonth.set(11);
    component.viewYear.set(2026);
    getCalendarSpy.mockClear();

    component.nextMonth();

    expect(component.viewMonth()).toBe(0);
    expect(component.viewYear()).toBe(2027);
    expect(getCalendarSpy).toHaveBeenCalledWith(1, 2027);
  });

  it('goToToday vuelve al mes actual', () => {
    component.viewMonth.set(0);
    component.viewYear.set(2020);
    getCalendarSpy.mockClear();

    component.goToToday();

    const now = new Date();
    expect(component.viewMonth()).toBe(now.getMonth());
    expect(component.viewYear()).toBe(now.getFullYear());
    expect(getCalendarSpy).toHaveBeenCalledWith(now.getMonth() + 1, now.getFullYear());
  });
});
