package com.Gestion.Arrayanes.DashboardDTO.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;


@Setter
@Getter
public class ClientApartmentDTO {

    private String clientName;
    private String clientLastName;
    private String apartmentName;
    private LocalDate arrivalDate;
    private LocalDate departureDate;

    public ClientApartmentDTO() {
    }

    public ClientApartmentDTO(String clientName, String clientLastName, String apartmentName, LocalDate arrivalDate, LocalDate departureDate) {
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.apartmentName = apartmentName;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
    }

}
