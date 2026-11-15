package com.Gestion.Arrayanes.Apartment.service;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface IApartmentService {

    ApartmentResponseDTO saveApartment(ApartmentRequestDTO apartmentRequestDTO);

    void deleteApartment(UUID id);

    List<ApartmentResponseDTO> getApartmentList();

    ApartmentResponseDTO findApartmentById(UUID id);

    ApartmentResponseDTO updateApartment(UUID id, ApartmentRequestDTO apartmentRequestDTO);

    Long getApartmentMaxNumber(UUID id);

    ApartmentResponseDTO editApartmentMaxNumber(UUID id, Long apartNumber);

    BigDecimal getPriceById(UUID id);

    ApartmentResponseDTO editPrice(UUID id, BigDecimal price);

    String getApartmentName(UUID id);

    ApartmentResponseDTO editApartName(UUID id, String apartName);

}
