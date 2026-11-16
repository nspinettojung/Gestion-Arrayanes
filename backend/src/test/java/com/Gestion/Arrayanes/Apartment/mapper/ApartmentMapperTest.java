package com.Gestion.Arrayanes.Apartment.mapper;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;
import com.Gestion.Arrayanes.Apartment.model.Apartment;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApartmentMapperTest {

    private final ApartmentMapper mapper = new ApartmentMapper();

    @Test
    void toEntityShouldMapAllFields() {
        ApartmentRequestDTO dto = new ApartmentRequestDTO("Arrayanes 1", 4L, new BigDecimal("10000"));

        Apartment apartment = mapper.toEntity(dto);

        assertEquals("Arrayanes 1", apartment.getApartmentName());
        assertEquals(4L, apartment.getApartmentMaxNumber());
        assertEquals(new BigDecimal("10000"), apartment.getPrice());
    }

    @Test
    void toEntityShouldTrimName() {
        ApartmentRequestDTO dto = new ApartmentRequestDTO("  Arrayanes 1  ", 4L, new BigDecimal("10000"));

        Apartment apartment = mapper.toEntity(dto);

        assertEquals("Arrayanes 1", apartment.getApartmentName());
    }

    @Test
    void toResponseDTOShouldMapAllFields() {
        Apartment apartment = new Apartment(UUID.randomUUID(), "Arrayanes 1", 4L, new BigDecimal("10000"));

        ApartmentResponseDTO dto = mapper.toResponseDTO(apartment);

        assertEquals(apartment.getId(), dto.getId());
        assertEquals("Arrayanes 1", dto.getApartmentName());
        assertEquals(4L, dto.getApartmentMaxNumber());
        assertEquals(new BigDecimal("10000"), dto.getPrice());
    }

    @Test
    void updateEntityShouldOverwriteFields() {
        Apartment apartment = new Apartment(UUID.randomUUID(), "Viejo", 2L, new BigDecimal("9000"));
        ApartmentRequestDTO dto = new ApartmentRequestDTO("Nuevo", 4L, new BigDecimal("12000"));

        mapper.updateEntity(apartment, dto);

        assertEquals("Nuevo", apartment.getApartmentName());
        assertEquals(4L, apartment.getApartmentMaxNumber());
        assertEquals(new BigDecimal("12000"), apartment.getPrice());
    }
}
