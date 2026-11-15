package com.Gestion.Arrayanes.Apartment.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ApartmentRequestDTO {

    @NotBlank(message = "El nombre del apartamento es obligatorio.")
    private String apartmentName;
    @NotNull(message = "La capacidad del apartamento es obligatoria.")
    @Positive(message = "La capacidad del apartamento debe ser mayor a cero.")
    private Long apartmentMaxNumber;
    @NotNull(message = "El precio del apartamento es obligatorio.")
    @Positive(message = "El precio del apartamento debe ser mayor a cero.")
    private BigDecimal price;

    public ApartmentRequestDTO() {
    }

    public ApartmentRequestDTO(String apartmentName, Long apartmentMaxNumber, BigDecimal price) {
        this.apartmentName = apartmentName;
        this.apartmentMaxNumber = apartmentMaxNumber;
        this.price = price;
    }

}
