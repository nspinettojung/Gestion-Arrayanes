package com.Gestion.Arrayanes.Apartment.repository;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class ApartmentRepositoryTest {

    @Autowired
    private ApartmentRepository apartmentRepository;

    @BeforeEach
    void setUp() {
        apartmentRepository.save(new Apartment(null, "Arrayanes 1", 4L, new BigDecimal("10000.00")));
    }

    @Test
    void shouldDetectExistingName() {
        assertTrue(apartmentRepository.existsByApartmentName("Arrayanes 1"));
        assertFalse(apartmentRepository.existsByApartmentName("Arrayanes 9"));
    }

    @Test
    void shouldFindNameIgnoringCase() {
        Optional<Apartment> found = apartmentRepository.findByApartmentNameIgnoreCase("arrayanes 1");

        assertTrue(found.isPresent());
        assertEquals("Arrayanes 1", found.get().getApartmentName());
    }

    @Test
    void shouldReturnEmptyWhenNameDoesNotExist() {
        assertTrue(apartmentRepository.findByApartmentNameIgnoreCase("Arrayanes 9").isEmpty());
    }
}
