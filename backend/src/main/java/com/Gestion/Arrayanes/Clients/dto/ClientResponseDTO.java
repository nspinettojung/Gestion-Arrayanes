package com.Gestion.Arrayanes.Clients.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class ClientResponseDTO {

    private UUID id;
    private String clientName;
    private String clientLastName;
    private String clientDni;
    private String clientPhone;
    private LocalDate lastCheckIn;

    public ClientResponseDTO() {
    }

    public ClientResponseDTO(UUID id, String clientName, String clientLastName, String clientDni, String clientPhone, LocalDate lastCheckIn) {
        this.id = id;
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.clientDni = clientDni;
        this.clientPhone = clientPhone;
        this.lastCheckIn = lastCheckIn;
    }

}
