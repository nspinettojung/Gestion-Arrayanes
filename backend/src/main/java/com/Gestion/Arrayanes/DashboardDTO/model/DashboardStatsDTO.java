package com.Gestion.Arrayanes.DashboardDTO.model;

/**
 * Indicadores del dashboard en una sola respuesta para evitar cinco
 * peticiones HTTP al cargar la pantalla principal.
 */
public record DashboardStatsDTO(
        long totalBookings,
        long totalApartments,
        double occupancy,
        double growth,
        long todayBookings
) {
}
