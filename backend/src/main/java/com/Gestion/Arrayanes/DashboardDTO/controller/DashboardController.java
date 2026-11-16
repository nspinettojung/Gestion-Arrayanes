package com.Gestion.Arrayanes.DashboardDTO.controller;

import com.Gestion.Arrayanes.DashboardDTO.model.CalendarDayDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.ClientApartmentDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.service.DashboardService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    private final Clock clock;

    public DashboardController(DashboardService dashboardService, Clock clock) {
        this.dashboardService = dashboardService;
        this.clock = clock;
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
            @RequestParam(defaultValue = "asc") String sortDir) {

        return ResponseEntity.ok(
                dashboardService.getDashboard(lastName, start, end, apartmentId, page, size, sortBy, sortDir)
        );
    }

    @GetMapping("/total/{entity}")
    public ResponseEntity<Map<String, Long>> getTotal(@PathVariable String entity){
        long total = dashboardService.getTotal(entity);
        return ResponseEntity.ok(Map.of( "total", total));
    }

    @GetMapping("/occupancy")
    public ResponseEntity<Double> getOccupancy(@RequestParam(required = false)
                                               @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date){

        if (date == null) {
            date = LocalDate.now(clock);
        }

        double occupancy = dashboardService.getOccupation(date);
        return ResponseEntity.ok(occupancy);
    }

    @GetMapping("/bookingsgrowth")
    public ResponseEntity<Double> getBookingsGrowth(){
        double growth = dashboardService.getBookingsGrowth();
        return ResponseEntity.ok(growth);
    }

    @GetMapping("/todaybookings")
    public ResponseEntity<Long> getTodayBookings(){
        long total = dashboardService.getTodayBookings();
        return ResponseEntity.ok(total);
    }

    @GetMapping("/monthlyArrivalsCount")
    public ResponseEntity<Long> getMonthlyArrivalsCount(
            @RequestParam int month,
            @RequestParam int year) {
        long arrivals = dashboardService.getArrivalsCountInMonth(LocalDate.of(year, month, 1));
        return ResponseEntity.ok(arrivals);
    }

    @GetMapping("/monthlyDeparturesCount")
    public ResponseEntity<Long> getMonthlyDeparturesCount(
            @RequestParam int month,
            @RequestParam int year) {
        long departures = dashboardService.getDeparturesCountInMonth(LocalDate.of(year, month, 1));
        return ResponseEntity.ok(departures);
    }

    @GetMapping("/monthlyArrivalsList")
    public ResponseEntity<List<ClientApartmentDTO>> getMonthlyArrivalsList(
            @RequestParam int month,
            @RequestParam int year) {
        List<ClientApartmentDTO> arrivals = dashboardService.getArrivalsListInMonth(LocalDate.of(year, month, 1));
        return ResponseEntity.ok(arrivals);
    }

    @GetMapping("/monthlyDeparturesList")
    public ResponseEntity<List<ClientApartmentDTO>> getMonthlyDeparturesList(
            @RequestParam int month,
            @RequestParam int year) {
        List<ClientApartmentDTO> departures = dashboardService.getDeparturesListInMonth(LocalDate.of(year, month, 1));
        return ResponseEntity.ok(departures);
    }

    @GetMapping("/arrivalsByDate")
    public ResponseEntity<List<ClientApartmentDTO>> getArrivalsByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        LocalDate date = LocalDate.of(year, month, day);
        List<ClientApartmentDTO> arrivals = dashboardService.getArrivalsByDate(date);
        return ResponseEntity.ok(arrivals);
    }

    @GetMapping("/departuresByDate")
    public ResponseEntity<List<ClientApartmentDTO>> getDeparturesByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        LocalDate date = LocalDate.of(year, month, day);
        List<ClientApartmentDTO> departures = dashboardService.getDeparturesByDate(date);
        return ResponseEntity.ok(departures);
    }

    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarDayDTO>> getCalendar(
            @RequestParam int year,
            @RequestParam int month) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.plusMonths(1).minusDays(1);

        List<CalendarDayDTO> calendar = dashboardService.getCalendar(start, end);
        return ResponseEntity.ok(calendar);
    }

    @GetMapping("/arrivalsCountByDate")
    public ResponseEntity<Long> getArrivalsCountByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        LocalDate date = LocalDate.of(year, month, day);
        long count = dashboardService.getArrivalsCountByDate(date);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/departuresCountByDate")
    public ResponseEntity<Long> getDeparturesCountByDate(
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam int day) {
        LocalDate date = LocalDate.of(year, month, day);
        long count = dashboardService.getDeparturesCountByDate(date);
        return ResponseEntity.ok(count);
    }


}
