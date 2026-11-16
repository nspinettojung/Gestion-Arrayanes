package com.Gestion.Arrayanes.Clients.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientRequestDTO {

    /**
     * DNI argentino (7 u 8 dígitos, con o sin puntos) o pasaporte alfanumérico.
     */
    public static final String DNI_PATTERN = "^[0-9A-Za-z.-]{6,15}$";
    /**
     * Teléfono con entre 6 y 20 caracteres y al menos 6 dígitos. Admite +, espacios,
     * guiones y paréntesis.
     */
    public static final String PHONE_PATTERN = "^(?=(?:\\D*\\d){6,})[+0-9()\\s-]{6,20}$";

    @NotBlank(message = "El nombre del cliente es obligatorio.")
    private String clientName;
    @NotBlank(message = "El apellido del cliente es obligatorio.")
    private String clientLastName;
    @NotBlank(message = "El DNI o pasaporte del cliente es obligatorio.")
    @Pattern(regexp = DNI_PATTERN,
            message = "El DNI o pasaporte debe tener entre 6 y 15 caracteres alfanuméricos.")
    private String clientDni;
    @NotBlank(message = "El teléfono del cliente es obligatorio.")
    @Pattern(regexp = PHONE_PATTERN,
            message = "El teléfono debe tener entre 6 y 20 caracteres y al menos 6 dígitos.")
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
