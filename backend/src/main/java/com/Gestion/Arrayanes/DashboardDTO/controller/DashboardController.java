package com.Gestion.Arrayanes.DashboardDTO.controller;

import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardStatsDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardSummaryDTO;
import com.Gestion.Arrayanes.DashboardDTO.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<List<DashboardDTO>> getDashboard(){
        return ResponseEntity.ok(dashboardService.getDashboard());
    }

    @GetMapping("/filter")
    public ResponseEntity<Page<DashboardDTO>> filterDashboard(
            @RequestParam(required = false) String lastName,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            @RequestParam(required = false) UUID apartmentId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "arrivalDate") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            @RequestParam(defaultValue = "false") boolean onlyActive) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(lastName, start, end, apartmentId, page, size, sortBy, sortDir, onlyActive)
        );
    }

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDTO> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Agregados del mes, calendario y movimientos del día en una sola llamada.
     * El día es opcional: sin él se devuelve el resumen mensual únicamente.
     */
    @GetMapping("/summary")
    public ResponseEntity<DashboardSummaryDTO> getSummary(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) Integer day) {

        // Validar aquí también: con el servicio mockeado (tests de controller)
        // nada más lanzaría DateTimeException y una fecha imposible daría 200.
        YearMonth.of(year, month);
        if (day != null) {
            LocalDate.of(year, month, day);
        }

        return ResponseEntity.ok(dashboardService.getSummary(year, month, day));
    }
}
