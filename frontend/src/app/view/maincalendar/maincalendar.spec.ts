import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { Dashboard } from '../../core/services/dashboard/dashboard';
import { toLocalIsoDate } from '../../core/utils/date';
import { Maincalendar } from './maincalendar';

describe('Maincalendar', () => {
  let component: Maincalendar;
  let fixture: ComponentFixture<Maincalendar>;
  let dashboard: Dashboard;
  let httpMock: HttpTestingController;
  let loadSummarySpy: ReturnType<typeof vi.spyOn>;

  const emptySummary = (month: number, year: number) => ({
    year,
    month,
    date: null,
    monthly: { arrivalsCount: 0, departuresCount: 0, arrivals: [], departures: [], calendar: [] },
    day: { arrivalsCount: 0, departuresCount: 0, arrivals: [], departures: [] },
  });

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Maincalendar],
      providers: [provideHttpClient(), provideHttpClientTesting()],
    }).compileComponents();

    fixture = TestBed.createComponent(Maincalendar);
    component = fixture.componentInstance;
    dashboard = TestBed.inject(Dashboard);
    httpMock = TestBed.inject(HttpTestingController);
    loadSummarySpy = vi.spyOn(dashboard, 'loadSummary').mockImplementation(() => undefined);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('pide el mes actual al backend al iniciar', () => {
    fixture.detectChanges();

    const now = new Date();
    expect(loadSummarySpy).toHaveBeenCalledWith(now.getMonth() + 1, now.getFullYear());
  });

  it('genera siempre una grilla de 42 días', () => {
    fixture.detectChanges();

    expect(component.cells()).toHaveLength(42);
  });

  it('marca como ocupados solo los días con reserva del resumen', async () => {
    const now = new Date();
    const occupiedIso = toLocalIsoDate(new Date(now.getFullYear(), now.getMonth(), 15));
    const freeIso = toLocalIsoDate(new Date(now.getFullYear(), now.getMonth(), 16));

    loadSummarySpy.mockRestore();
    fixture.detectChanges();

    const request = httpMock.expectOne((req) => req.url.includes('/dashboard/summary'));
    expect(request.request.params.get('month')).toBe(String(now.getMonth() + 1));
    expect(request.request.params.has('day')).toBe(false);
    request.flush({
      ...emptySummary(now.getMonth() + 1, now.getFullYear()),
      monthly: {
        ...emptySummary(now.getMonth() + 1, now.getFullYear()).monthly,
        calendar: [
          { date: occupiedIso, hasBooking: true },
          { date: freeIso, hasBooking: false },
        ],
      },
    });
    // httpResource resuelve el valor en una tarea asíncrona tras la respuesta.
    await new Promise<void>((resolve) => setTimeout(resolve));

    const cells = component.cells();
    expect(cells.find((cell) => cell.date === occupiedIso)?.hasBooking).toBe(true);
    expect(cells.find((cell) => cell.date === freeIso)?.hasBooking).toBe(false);
  });

  it('retroceder desde enero va a diciembre del año anterior', () => {
    component.viewMonth.set(0);
    component.viewYear.set(2026);
    loadSummarySpy.mockClear();

    component.previousMonth();

    expect(component.viewMonth()).toBe(11);
    expect(component.viewYear()).toBe(2025);
    expect(loadSummarySpy).toHaveBeenCalledWith(12, 2025);
  });

  it('avanzar desde diciembre va a enero del año siguiente', () => {
    component.viewMonth.set(11);
    component.viewYear.set(2026);
    loadSummarySpy.mockClear();

    component.nextMonth();

    expect(component.viewMonth()).toBe(0);
    expect(component.viewYear()).toBe(2027);
    expect(loadSummarySpy).toHaveBeenCalledWith(1, 2027);
  });

  it('goToToday vuelve al mes actual', () => {
    component.viewMonth.set(0);
    component.viewYear.set(2020);
    loadSummarySpy.mockClear();

    component.goToToday();

    const now = new Date();
    expect(component.viewMonth()).toBe(now.getMonth());
    expect(component.viewYear()).toBe(now.getFullYear());
    expect(loadSummarySpy).toHaveBeenCalledWith(now.getMonth() + 1, now.getFullYear());
  });
});
