package com.Gestion.Arrayanes.Clients.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientRequestDTO {

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String clientName;
    @NotBlank(message = "El apellido del cliente es obligatorio.")
    private String clientLastName;
    @NotBlank(message = "El DNI del cliente es obligatorio.")
    private String clientDni;
    @NotBlank(message = "El teléfono del cliente es obligatorio.")
    private String clientPhone;

    public ClientRequestDTO() {
    }

    public ClientRequestDTO(String clientName, String clientLastName, String clientDni, String clientPhone) {
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.clientDni = clientDni;
        this.clientPhone = clientPhone;
    }

}
