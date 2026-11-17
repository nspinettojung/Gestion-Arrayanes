package com.Gestion.Arrayanes.DashboardDTO.model;

import java.util.List;

/** Movimientos de un día concreto. Sin día solicitado se devuelve VACIO. */
public record DaySummaryDTO(
        long arrivalsCount,
        long departuresCount,
        List<ClientApartmentDTO> arrivals,
        List<ClientApartmentDTO> departures
) {

    public static final DaySummaryDTO VACIO =
            new DaySummaryDTO(0L, 0L, List.of(), List.of());
}
