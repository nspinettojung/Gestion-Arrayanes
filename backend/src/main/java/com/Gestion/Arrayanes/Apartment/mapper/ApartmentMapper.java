package com.Gestion.Arrayanes.Apartment.mapper;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;
import com.Gestion.Arrayanes.Apartment.model.Apartment;
import org.springframework.stereotype.Component;

@Component
public class ApartmentMapper {

    public Apartment toEntity(ApartmentRequestDTO dto){

        Apartment apartment = new Apartment();

        apartment.setApartmentName(dto.getApartmentName());
        apartment.setApartmentMaxNumber(dto.getApartmentMaxNumber());
        apartment.setPrice(dto.getPrice());

        return apartment;
    }

    public ApartmentResponseDTO toResponseDTO(Apartment apartment){

        ApartmentResponseDTO dto = new ApartmentResponseDTO();

        dto.setId(apartment.getId());
        dto.setApartmentName(apartment.getApartmentName());
        dto.setApartmentMaxNumber(apartment.getApartmentMaxNumber());
        dto.setPrice(apartment.getPrice());

        return dto;
    }

    public void updateEntity(
            Apartment apartment,
            ApartmentRequestDTO dto) {

        apartment.setApartmentName(dto.getApartmentName());
        apartment.setApartmentMaxNumber(dto.getApartmentMaxNumber());
        apartment.setPrice(dto.getPrice());
    }
}
