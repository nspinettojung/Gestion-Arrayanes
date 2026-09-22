import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal, Signal } from '@angular/core';
import { Booking } from '../../core/services/booking/booking';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { Calendar } from '../assets/calendar/calendar';
import { IDashboard } from '../../core/interface/dashboard/dashboard';
import { InitialsPipe } from '../assets/pipes/initials-pipe';

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
  selector: 'app-mainbooking',
  imports: [CommonModule, Calendar, InitialsPipe],
  templateUrl: './mainbooking.html',
  styleUrl: './mainbooking.css',
})
export class Mainbooking implements OnInit {
  private bookingService = inject(Booking);
  private dashboardService = inject(Dashboard);
  today = signal<string>('');
  days = computed(() => this.bookingService.days());
  selectedDay: number | null = null;
  dashboard = computed(() => this.dashboardService.dashboardSignal());
  monthlyArrivals = computed(() => this.dashboardService.monthlyArrivals());
  monthlyDepartures = computed(() => this.dashboardService.monthlyDepartures());
  monthlyArrivalsList = computed(() => this.dashboardService.monthlyArrivalsList());
  monthlyDeparturesList = computed(() => this.dashboardService.monthlyDeparturesList());
  arrivalsByDate = computed(() => this.dashboardService.arrivalsByDate());
  departuresByDate = computed(() => this.dashboardService.departuresByDate());
  arrivalsCount = computed(() => this.dashboardService.arrivalsCount());
  departuresCount = computed(() => this.dashboardService.departuresCount());

  monthlyArrivalsDate: IDashboard[] = [];
  monthlyDeparturesDate: IDashboard[] = [];
  selectedDayReservations: IDashboard[] = [];
  currentMonth!: number;
  currentYear!: number;

  ngOnInit(): void {
    const now = new Date();
    const month = now.getMonth() + 1;
    const year = now.getFullYear();
    const day = now.getDate();

    this.bookingService.getBookings();
    this.bookingService.getCurrentDate().subscribe((date) => this.today.set(date));
    this.dashboardService.loadDashboard();
    this.bookingService.getCalendar(year, month);
    this.dashboardService.getArrivalsByDate(year, month, day);
    this.dashboardService.getDeparturesByDate(year, month, day);
    this.dashboardService.getMonthlyArrivals(month, year);
    this.dashboardService.getMonthlyDepartures(month, year);
    this.dashboardService.getArrivalsCountByDate(year, month, day);
    this.dashboardService.getDeparturesCountByDate(year, month, day);
  }

  onDaySelected(date: Date) {
    this.selectedDay = date.getDate();

    const year = date.getFullYear();
    const month = date.getMonth() + 1;
    const day = date.getDate();

    this.dashboardService.getArrivalsByDate(year, month, day);
    this.dashboardService.getDeparturesByDate(year, month, day);
    this.dashboardService.getArrivalsCountByDate(year, month, day);
    this.dashboardService.getDeparturesCountByDate(year, month, day);
  }
}
