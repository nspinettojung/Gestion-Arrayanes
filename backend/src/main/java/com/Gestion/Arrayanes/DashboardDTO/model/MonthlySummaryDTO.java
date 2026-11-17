package com.Gestion.Arrayanes.DashboardDTO.model;

import java.util.List;

/** Indicadores y listados de un mes, más el calendario de ocupación. */
public record MonthlySummaryDTO(
        long arrivalsCount,
        long departuresCount,
        List<ClientApartmentDTO> arrivals,
        List<ClientApartmentDTO> departures,
        List<CalendarDayDTO> calendar
) {
}
