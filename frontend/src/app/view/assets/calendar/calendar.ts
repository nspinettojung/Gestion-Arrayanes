import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  computed,
  EventEmitter,
  inject,
  Output,
  signal,
} from '@angular/core';
import { Dashboard } from '../../../core/services/dashboard/dashboard';
import { IDashboard } from '../../../core/interface/dashboard/dashboard';
import { Booking } from '../../../core/services/booking/booking';
import { toLocalIsoDate } from '../../../core/utils/date';

export interface CalendarDay {
  day: number;
  currentMonth: boolean;
  date: Date;
  reservations: {
    type: 'arrival' | 'departure';
    booking: IDashboard;
  }[];
}

@Component({
  selector: 'gda-calendar',
  imports: [CommonModule],
  templateUrl: './calendar.html',
  styleUrl: './calendar.scss',
})
export class Calendar {
  private dashboardService = inject(Dashboard);
  private cdr = inject(ChangeDetectorRef);
  arrivalsCount = computed(() => this.dashboardService.arrivalsCount());
  departuresCount = computed(() => this.dashboardService.departuresCount());
  currentDate = new Date();
  currentMonth!: number;
  currentYear!: number;
  @Output() daySelected = new EventEmitter<Date>();
  selectedDay: number | null = null;
  monthName = '';

  monthlyArrivalsDate: IDashboard[] = [];
  monthlyDeparturesDate: IDashboard[] = [];
  selectedDayReservations: IDashboard[] = [];

  days: CalendarDay[] = [];

  readonly weekDays = ['Dom', 'Lun', 'Mar', 'Mié', 'Jue', 'Vie', 'Sáb'];

  ngOnInit(): void {
    this.currentMonth = this.currentDate.getMonth();
    this.currentYear = this.currentDate.getFullYear();
    this.loadCalendar();
  }

  loadCalendar(): void {
    this.days = [];
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

    for (let i = firstWeekDay - 1; i >= 0; i--) {
      this.days.push({
        day: prevMonthLastDay - i,
        currentMonth: false,
        date: new Date(this.currentYear, this.currentMonth - 1, prevMonthLastDay - i),
        reservations: [],
      });
    }

    for (let day = 1; day <= daysInMonth; day++) {
      this.days.push({
        day,
        currentMonth: true,
        date: new Date(this.currentYear, this.currentMonth, day),
        reservations: [],
      });
    }

    const remaining = 42 - this.days.length;
    for (let day = 1; day <= remaining; day++) {
      this.days.push({
        day,
        currentMonth: false,
        date: new Date(this.currentYear, this.currentMonth + 1, day),
        reservations: [],
      });
    }

    this.loadReservations();
  }

  loadReservations(): void {
    this.dashboardService
      .getMonthlyArrivalsList(this.currentMonth + 1, this.currentYear)
      .subscribe((arrivals: IDashboard[]) => {
        arrivals.forEach((b: IDashboard) => {
          const arrivalStr = b.arrivalDate;
          this.days.forEach((d) => {
            if (d.currentMonth && formatDate(d.date) === arrivalStr) {
              d.reservations.push({ type: 'arrival', booking: b });
            }
          });
        });
        this.cdr.detectChanges();
      });

    this.dashboardService
      .getMonthlyDeparturesList(this.currentMonth + 1, this.currentYear)
      .subscribe((departures: IDashboard[]) => {
        departures.forEach((b: IDashboard) => {
          const departureStr = b.departureDate;
          this.days.forEach((d) => {
            if (d.currentMonth && formatDate(d.date) === departureStr) {
              d.reservations.push({ type: 'departure', booking: b });
            }
          });
        });
        this.cdr.detectChanges();
      });
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
      this.selectedDayReservations = day.reservations.map((r) => r.booking);
    }
  }
}
function formatDate(date: Date): string {
  return toLocalIsoDate(date);
}
