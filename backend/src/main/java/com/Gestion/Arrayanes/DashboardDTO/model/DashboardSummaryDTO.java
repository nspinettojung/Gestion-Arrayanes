package com.Gestion.Arrayanes.DashboardDTO.model;

import java.time.LocalDate;

/**
 * Respuesta unificada del panel: agregados del mes, calendario y, cuando se
 * pide un día, los movimientos de esa fecha. Reemplaza los endpoints que
 * devolvían un único número o una única lista.
 */
public record DashboardSummaryDTO(
        int year,
        int month,
        LocalDate date,
        MonthlySummaryDTO monthly,
        DaySummaryDTO day
) {
}
