import { CommonModule } from '@angular/common';
import { Component, computed, EventEmitter, inject, OnInit, Output, signal } from '@angular/core';
import { Dashboard } from '../../../core/services/dashboard/dashboard';
import { ClientApartmentDTO } from '../../../core/interface/clients/client-apartment-dto';
import { toLocalIsoDate } from '../../../core/utils/date';

export interface CalendarDay {
  day: number;
  currentMonth: boolean;
  date: Date;
  reservations: {
    type: 'arrival' | 'departure';
    booking: ClientApartmentDTO;
  }[];
}

@Component({
  selector: 'gda-calendar',
  imports: [CommonModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss',
})
export class Calendar implements OnInit {
  private dashboardService = inject(Dashboard);
  arrivalsCount = computed(() => this.dashboardService.arrivalsCount());
  departuresCount = computed(() => this.dashboardService.departuresCount());
  currentDate = new Date();
  currentMonth!: number;
  currentYear!: number;
  @Output() daySelected = new EventEmitter<Date>();
  selectedDay: number | null = null;
  monthName = '';

  private readonly baseDays = signal<CalendarDay[]>([]);

  /** Los días se derivan del mes visible y de las listas del resumen mensual. */
  readonly days = computed(() => {
    const arrivals = this.dashboardService.monthlyArrivalsList();
    const departures = this.dashboardService.monthlyDeparturesList();

    return this.baseDays().map((day) => {
      const reservations: CalendarDay['reservations'] = [];

      arrivals.forEach((b) => {
        if (day.currentMonth && formatDate(day.date) === b.arrivalDate) {
          reservations.push({ type: 'arrival', booking: b });
        }
      });
      departures.forEach((b) => {
        if (day.currentMonth && formatDate(day.date) === b.departureDate) {
          reservations.push({ type: 'departure', booking: b });
        }
      });

      return { ...day, reservations };
    });
  });

  readonly weekDays = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  ngOnInit(): void {
    this.currentMonth = this.currentDate.getMonth();
    this.currentYear = this.currentDate.getFullYear();
    this.loadCalendar();
  }

  loadCalendar(): void {
    this.baseDays.set([]);
    this.currentMonth = this.currentDate.getMonth();
    this.currentYear = this.currentDate.getFullYear();

    this.monthName = this.currentDate.toLocaleDateString('es-AR', {
      month: 'long',
      year: 'numeric',
    });

    const firstDay = new Date(this.currentYear, this.currentMonth, 1);
    const lastDay = new Date(this.currentYear, this.currentMonth + 1, 0);
    const firstWeekDay = firstDay.getDay();
    const daysInMonth = lastDay.getDate();

    const prevMonthLastDay = new Date(this.currentYear, this.currentMonth, 0).getDate();

    const built: CalendarDay[] = [];

    for (let i = firstWeekDay - 1; i >= 0; i--) {
      built.push({
        day: prevMonthLastDay - i,
        currentMonth: false,
        date: new Date(this.currentYear, this.currentMonth - 1, prevMonthLastDay - i),
        reservations: [],
      });
    }

    for (let day = 1; day <= daysInMonth; day++) {
      built.push({
        day,
        currentMonth: true,
        date: new Date(this.currentYear, this.currentMonth, day),
        reservations: [],
      });
    }

    const remaining = 42 - built.length;
    for (let day = 1; day <= remaining; day++) {
      built.push({
        day,
        currentMonth: false,
        date: new Date(this.currentYear, this.currentMonth + 1, day),
        reservations: [],
      });
    }

    this.baseDays.set(built);
    this.dashboardService.loadSummary(this.currentMonth + 1, this.currentYear);
  }

  previousMonth(): void {
    this.currentDate = new Date(this.currentYear, this.currentMonth - 1, 1);

    this.loadCalendar();
  }

  nextMonth(): void {
    this.currentDate = new Date(this.currentYear, this.currentMonth + 1, 1);

    this.loadCalendar();
  }

  goToToday(): void {
    this.currentDate = new Date();
    this.loadCalendar();
  }

  isToday(date: Date): boolean {
    const today = new Date();

    return (
      date.getDate() === today.getDate() &&
      date.getMonth() === today.getMonth() &&
      date.getFullYear() === today.getFullYear()
    );
  }

  selectDay(day: CalendarDay): void {
    if (day.currentMonth && day.day) {
      this.selectedDay = day.day;
      this.daySelected.emit(day.date);
    }
  }
}
function formatDate(date: Date): string {
  return toLocalIsoDate(date);
}
