import { CommonModule } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { Booking } from '../../core/services/booking/booking';
import { Dashboard } from '../../core/services/dashboard/dashboard';
import { Calendar } from '../assets/calendar/calendar';
import { InitialsPipe } from '../assets/pipes/initials-pipe';

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
  selectedDay: number | null = null;
  monthlyArrivals = computed(() => this.dashboardService.monthlyArrivals());
  monthlyDepartures = computed(() => this.dashboardService.monthlyDepartures());
  monthlyArrivalsList = computed(() => this.dashboardService.monthlyArrivalsList());
  monthlyDeparturesList = computed(() => this.dashboardService.monthlyDeparturesList());
  arrivalsByDate = computed(() => this.dashboardService.arrivalsByDate());
  departuresByDate = computed(() => this.dashboardService.departuresByDate());
  arrivalsCount = computed(() => this.dashboardService.arrivalsCount());
  departuresCount = computed(() => this.dashboardService.departuresCount());

  currentMonth!: number;
  currentYear!: number;

  ngOnInit(): void {
    const now = new Date();
    const month = now.getMonth() + 1;
    const year = now.getFullYear();
    const day = now.getDate();

    this.bookingService.getBookings();
    this.bookingService.getCurrentDate().subscribe((date) => this.today.set(date));
    this.dashboardService.loadSummary(month, year, day);
  }

  onDaySelected(date: Date) {
    this.selectedDay = date.getDate();
    this.dashboardService.loadSummary(date.getMonth() + 1, date.getFullYear(), date.getDate());
  }
}
