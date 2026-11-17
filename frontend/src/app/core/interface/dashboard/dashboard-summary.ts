import { CalendarDay } from './calendar-day';
import { ClientApartmentDTO } from '../clients/client-apartment-dto';

export interface DaySummary {
  arrivalsCount: number;
  departuresCount: number;
  arrivals: ClientApartmentDTO[];
  departures: ClientApartmentDTO[];
}

export interface MonthlySummary {
  arrivalsCount: number;
  departuresCount: number;
  arrivals: ClientApartmentDTO[];
  departures: ClientApartmentDTO[];
  calendar: CalendarDay[];
}

export interface DashboardSummary {
  year: number;
  month: number;
  date: string | null;
  monthly: MonthlySummary;
  day: DaySummary;
}
