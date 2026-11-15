package com.Gestion.Arrayanes.DashboardDTO.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Getter
public class CalendarDayDTO {

    private LocalDate date;
    private boolean hasBooking;

    public CalendarDayDTO(LocalDate date, boolean hasBooking) {
        this.date = date;
        this.hasBooking = hasBooking;
    }

}
