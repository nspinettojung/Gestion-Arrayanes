import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { toLocalIsoDate } from '../../core/utils/date';

interface MonthCell {
  day: number;
  date: string;
  currentMonth: boolean;
  hasBooking: boolean;
  isToday: boolean;
}

@Component({
  selector: 'app-maincalendar',
  imports: [],
  templateUrl: './maincalendar.html',
  styleUrl: './maincalendar.scss',
})
export class Maincalendar implements OnInit {
  private dashboardService = inject(Dashboard);

  readonly weekDays = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  viewYear = signal(new Date().getFullYear());
  viewMonth = signal(new Date().getMonth());

  loading = this.dashboardService.loadingCalendar;

  monthName = computed(() =>
    new Date(this.viewYear(), this.viewMonth(), 1).toLocaleDateString('es-AR', {
      month: 'long',
      year: 'numeric',
    }),
  );

  /** Días (ISO) del mes visible con al menos una reserva en curso. */
  occupiedDates = computed(() => {
    const dates = new Set<string>();

    this.dashboardService.calendarDays().forEach((day) => {
      if (day.hasBooking) {
        dates.add(day.date);
      }
    });

    return dates;
  });

  cells = computed<MonthCell[]>(() => {
    const year = this.viewYear();
    const month = this.viewMonth();
    const today = toLocalIsoDate();
    const occupied = this.occupiedDates();

    const firstWeekDay = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const prevMonthDays = new Date(year, month, 0).getDate();

    const build = (day: number, offsetMonth: number, currentMonth: boolean): MonthCell => {
      const date = toLocalIsoDate(new Date(year, month + offsetMonth, day));

      return {
        day,
        date,
        currentMonth,
        hasBooking: occupied.has(date),
        isToday: date === today,
      };
    };

    const result: MonthCell[] = [];

    for (let i = firstWeekDay - 1; i >= 0; i--) {
      result.push(build(prevMonthDays - i, -1, false));
    }
    for (let day = 1; day <= daysInMonth; day++) {
      result.push(build(day, 0, true));
    }
    for (let day = 1; result.length < 42; day++) {
      result.push(build(day, 1, false));
    }

    return result;
  });

  ngOnInit(): void {
    this.loadCalendar();
  }

  private loadCalendar(): void {
    this.dashboardService.loadSummary(this.viewMonth() + 1, this.viewYear());
  }

  previousMonth(): void {
    if (this.viewMonth() === 0) {
      this.viewMonth.set(11);
      this.viewYear.update((year) => year - 1);
    } else {
      this.viewMonth.update((month) => month - 1);
    }

    this.loadCalendar();
  }

  nextMonth(): void {
    if (this.viewMonth() === 11) {
      this.viewMonth.set(0);
      this.viewYear.update((year) => year + 1);
    } else {
      this.viewMonth.update((month) => month + 1);
    }

    this.loadCalendar();
  }

  goToToday(): void {
    const now = new Date();
    this.viewYear.set(now.getFullYear());
    this.viewMonth.set(now.getMonth());
    this.loadCalendar();
  }
}
