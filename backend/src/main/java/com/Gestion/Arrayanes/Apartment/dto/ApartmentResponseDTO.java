package com.Gestion.Arrayanes.Apartment.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
public class ApartmentResponseDTO {

    private UUID id;
    private String apartmentName;
    private Long apartmentMaxNumber;
    private BigDecimal price;

    public ApartmentResponseDTO() {
    }

    public ApartmentResponseDTO(UUID id, String apartmentName, Long apartmentMaxNumber, BigDecimal price) {
        this.id = id;
        this.apartmentName = apartmentName;
        this.apartmentMaxNumber = apartmentMaxNumber;
        this.price = price;
    }

}
