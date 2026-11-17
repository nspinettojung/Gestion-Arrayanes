package com.Gestion.Arrayanes.DashboardDTO.controller;

import com.Gestion.Arrayanes.DashboardDTO.model.CalendarDayDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.ClientApartmentDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardStatsDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DashboardSummaryDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.DaySummaryDTO;
import com.Gestion.Arrayanes.DashboardDTO.model.MonthlySummaryDTO;
import com.Gestion.Arrayanes.DashboardDTO.service.DashboardService;
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
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
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

        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].clientName").value("Ana"))
                .andExpect(jsonPath("$[0].transactionCode").value("RS-2026-0001"))
                .andExpect(jsonPath("$[0].apartmentName").value("Arrayanes 1"))
                .andExpect(jsonPath("$[0].version").value(4));
    }

    @Test
    void filterDashboardShouldReturnPage() throws Exception {
        when(dashboardService.getDashboard(
                any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(dashboardDto())));

        mockMvc.perform(get("/api/v1/dashboard/filter")
                        .param("lastName", "Perez")
                        .param("page", "0")
                        .param("size", "5")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(dashboardDto().getId().toString()));
    }

    @Test
    void filterDashboardShouldForwardOnlyActiveFlag() throws Exception {
        when(dashboardService.getDashboard(
                any(), any(), any(), any(), anyInt(), anyInt(), anyString(), anyString(), anyBoolean()))
                .thenReturn(new PageImpl<>(List.of(dashboardDto())));

        mockMvc.perform(get("/api/v1/dashboard/filter")
                        .param("onlyActive", "true"))
                .andExpect(status().isOk());

        verify(dashboardService).getDashboard(null, null, null, null, 0, 10, "arrivalDate", "asc", true);
    }

    @Test
    void getStatsShouldReturnAggregatedValues() throws Exception {
        when(dashboardService.getStats())
                .thenReturn(new DashboardStatsDTO(7, 4, 50.0, 12.5, 3));

        mockMvc.perform(get("/api/v1/dashboard/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBookings").value(7))
                .andExpect(jsonPath("$.totalApartments").value(4))
                .andExpect(jsonPath("$.occupancy").value(50.0))
                .andExpect(jsonPath("$.growth").value(12.5))
                .andExpect(jsonPath("$.todayBookings").value(3));
    }

    @Test
    void summaryShouldReturnMonthCalendarAndDayAggregates() throws Exception {
        when(dashboardService.getSummary(2026, 11, 14)).thenReturn(new DashboardSummaryDTO(
                2026, 11, TODAY,
                new MonthlySummaryDTO(
                        4L, 6L,
                        List.of(clientApartmentDto()), List.of(clientApartmentDto()),
                        List.of(new CalendarDayDTO(TODAY, true))),
                new DaySummaryDTO(
                        2L, 1L,
                        List.of(clientApartmentDto()), List.of(clientApartmentDto()))));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .param("year", "2026").param("month", "11").param("day", "14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(2026))
                .andExpect(jsonPath("$.month").value(11))
                .andExpect(jsonPath("$.date").value("2026-11-14"))
                .andExpect(jsonPath("$.monthly.arrivalsCount").value(4))
                .andExpect(jsonPath("$.monthly.departuresCount").value(6))
                .andExpect(jsonPath("$.monthly.arrivals[0].clientName").value("Ana"))
                .andExpect(jsonPath("$.monthly.calendar[0].hasBooking").value(true))
                .andExpect(jsonPath("$.day.arrivalsCount").value(2))
                .andExpect(jsonPath("$.day.departuresCount").value(1))
                .andExpect(jsonPath("$.day.departures[0].apartmentName").value("Arrayanes 1"));
    }

    @Test
    void summaryWithoutDayShouldReturnEmptyDayAndForwardNull() throws Exception {
        when(dashboardService.getSummary(2026, 11, null)).thenReturn(new DashboardSummaryDTO(
                2026, 11, null,
                new MonthlySummaryDTO(4L, 6L, List.of(), List.of(), List.of()),
                DaySummaryDTO.VACIO));

        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .param("year", "2026").param("month", "11"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.monthly.calendar").isEmpty())
                .andExpect(jsonPath("$.day.arrivalsCount").value(0));

        verify(dashboardService).getSummary(2026, 11, null);
    }

    @Test
    void summaryShouldRejectImpossibleDate() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/summary")
                        .param("year", "2026").param("month", "13").param("day", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private DashboardDTO dashboardDto() {
        return new DashboardDTO(
                UUID.fromString("44444444-4444-4444-4444-444444444444"),
                "RS-2026-0001",
                UUID.fromString("22222222-2222-2222-2222-222222222222"),
                "Ana",
                "Perez",
                UUID.fromString("33333333-3333-3333-3333-333333333333"),
                "Arrayanes 1",
                LocalDate.of(2026, 11, 10),
                LocalDate.of(2026, 11, 12),
                2L,
                2L,
                new BigDecimal("30000.00"),
                4L
        );
    }

    private ClientApartmentDTO clientApartmentDto() {
        return new ClientApartmentDTO(
                "Ana", "Perez", "Arrayanes 1",
                LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
    }
}
