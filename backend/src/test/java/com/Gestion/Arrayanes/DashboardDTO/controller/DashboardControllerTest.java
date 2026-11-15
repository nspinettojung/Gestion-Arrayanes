package com.Gestion.Arrayanes.DashboardDTO.controller;

import com.Gestion.Arrayanes.DashboardDTO.model.CalendarDayDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.ClientApartmentDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.service.DashboardService;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
class DashboardControllerTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 14);

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @Test
    void getDashboardShouldReturnBookings() throws Exception {
        when(dashboardService.getDashboard()).thenReturn(List.of(dashboardDto()));

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Ana"))
                .andExpect(jsonPath("$[0].apartmentName").value("Arrayanes 1"));
    }

    @Test
    void filterDashboardShouldReturnPage() throws Exception {
        when(dashboardService.getDashboard(
                any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(new PageImpl<>(List.of(dashboardDto())));

        mockMvc.perform(get("/api/dashboard/filter")
                        .param("lastName", "Perez")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dashboardDto().getId().toString()));
    }

    @Test
    void getTotalShouldReturnTotalMap() throws Exception {
        when(dashboardService.getTotal("bookings")).thenReturn(5L);

        mockMvc.perform(get("/api/dashboard/total/{entity}", "bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(5));
    }

    @Test
    void getTotalShouldMapUnsupportedEntityTo409() throws Exception {
        when(dashboardService.getTotal("clients"))
                .thenThrow(new GlobalCustomException("Entidad no soportada: clients"));

        mockMvc.perform(get("/api/dashboard/total/{entity}", "clients"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getOccupancyShouldReturnPercentage() throws Exception {
        when(dashboardService.getOccupation(TODAY)).thenReturn(75.0);

        mockMvc.perform(get("/api/dashboard/occupancy").param("date", "2026-11-14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(75.0));
    }

    @Test
    void getOccupancyShouldRejectInvalidDate() throws Exception {
        mockMvc.perform(get("/api/dashboard/occupancy").param("date", "not-a-date"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void scalarEndpointsShouldDelegateToService() throws Exception {
        when(dashboardService.getBookingsGrowth()).thenReturn(12.5);
        when(dashboardService.getTodayBookings()).thenReturn(3L);
        when(dashboardService.getArrivalsCountInMonth(LocalDate.of(2026, 11, 1))).thenReturn(4L);
        when(dashboardService.getDeparturesCountInMonth(LocalDate.of(2026, 11, 1))).thenReturn(6L);
        when(dashboardService.getArrivalsCountByDate(TODAY)).thenReturn(2L);
        when(dashboardService.getDeparturesCountByDate(TODAY)).thenReturn(1L);

        mockMvc.perform(get("/api/dashboard/bookingsgrowth"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(12.5));

        mockMvc.perform(get("/api/dashboard/todaybookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(3));

        mockMvc.perform(get("/api/dashboard/monthlyArrivalsCount")
                        .param("month", "11").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(4));

        mockMvc.perform(get("/api/dashboard/monthlyDeparturesCount")
                        .param("month", "11").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(6));

        mockMvc.perform(get("/api/dashboard/arrivalsCountByDate")
                        .param("year", "2026").param("month", "11").param("day", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(2));

        mockMvc.perform(get("/api/dashboard/departuresCountByDate")
                        .param("year", "2026").param("month", "11").param("day", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1));
    }

    @Test
    void listEndpointsShouldReturnMappedBookings() throws Exception {
        when(dashboardService.getArrivalsListInMonth(LocalDate.of(2026, 11, 1)))
                .thenReturn(List.of(clientApartmentDto()));
        when(dashboardService.getDeparturesListInMonth(LocalDate.of(2026, 11, 1)))
                .thenReturn(List.of(clientApartmentDto()));
        when(dashboardService.getArrivalsByDate(TODAY)).thenReturn(List.of(clientApartmentDto()));
        when(dashboardService.getDeparturesByDate(TODAY)).thenReturn(List.of(clientApartmentDto()));

        mockMvc.perform(get("/api/dashboard/monthlyArrivalsList")
                        .param("month", "11").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Ana"));

        mockMvc.perform(get("/api/dashboard/monthlyDeparturesList")
                        .param("month", "11").param("year", "2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].apartmentName").value("Arrayanes 1"));

        mockMvc.perform(get("/api/dashboard/arrivalsByDate")
                        .param("year", "2026").param("month", "11").param("day", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientLastName").value("Perez"));

        mockMvc.perform(get("/api/dashboard/departuresByDate")
                        .param("year", "2026").param("month", "11").param("day", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Ana"));
    }

    @Test
    void getCalendarShouldReturnDays() throws Exception {
        when(dashboardService.getCalendar(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 30)))
                .thenReturn(List.of(new CalendarDayDTO(LocalDate.of(2026, 11, 1), true)));

        mockMvc.perform(get("/api/dashboard/calendar")
                        .param("year", "2026").param("month", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-11-01"))
                .andExpect(jsonPath("$[0].hasBooking").value(true));
    }

    @Test
    void arrivalsByDateShouldRejectImpossibleDate() throws Exception {
        mockMvc.perform(get("/api/dashboard/arrivalsByDate")
                        .param("year", "2026").param("month", "13").param("day", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private DashboardDTO dashboardDto() {
        return new DashboardDTO(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Ana",
                "Perez",
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Arrayanes 1",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                2L,
                2L,
                new BigDecimal("30000.00")
        );
    }

    private ClientApartmentDTO clientApartmentDto() {
        return new ClientApartmentDTO(
                "Ana", "Perez", "Arrayanes 1",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
    }
}
