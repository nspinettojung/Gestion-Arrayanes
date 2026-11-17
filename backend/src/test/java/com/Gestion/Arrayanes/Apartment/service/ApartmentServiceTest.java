package com.Gestion.Arrayanes.Apartment.service;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;
import com.Gestion.Arrayanes.Apartment.mapper.ApartmentMapper;
import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApartmentServiceTest {

    private static final BigDecimal PRICE = new BigDecimal("9999.00");

    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private ApartmentMapper apartmentMapper;
    @Mock
    private BookingRepository bookingRepository;

    @InjectMocks
    private ApartmentService apartmentService;

    private UUID apartmentId;
    private Apartment apartment;
    private ApartmentRequestDTO request;
    private ApartmentResponseDTO response;

    @BeforeEach
    void setUp() {
        apartmentId = UUID.randomUUID();
        apartment = new Apartment(apartmentId, "Arrayanes 1", 4L, PRICE);
        request = new ApartmentRequestDTO("Arrayanes 1", 4L, PRICE);
        response = new ApartmentResponseDTO(apartmentId, "Arrayanes 1", 4L, PRICE);
    }

    @Test
    void saveApartmentShouldPersistNewApartment() {
        when(apartmentRepository.findByApartmentNameIgnoreCase("Arrayanes 1")).thenReturn(Optional.empty());
        when(apartmentMapper.toEntity(request)).thenReturn(apartment);
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        ApartmentResponseDTO result = apartmentService.saveApartment(request);

        assertEquals(response, result);
        verify(apartmentRepository).save(apartment);
    }

    @Test
    void saveApartmentShouldRejectDuplicatedName() {
        Apartment other = new Apartment(UUID.randomUUID(), "arrayanes 1", 2L, PRICE);
        when(apartmentRepository.findByApartmentNameIgnoreCase("Arrayanes 1")).thenReturn(Optional.of(other));

        assertThrows(GlobalCustomException.class, () -> apartmentService.saveApartment(request));

        verify(apartmentRepository, never()).save(any(Apartment.class));
    }

    @Test
    void saveApartmentShouldRejectBlankName() {
        ApartmentRequestDTO blank = new ApartmentRequestDTO("   ", 4L, PRICE);

        assertThrows(BadRequestException.class, () -> apartmentService.saveApartment(blank));

        verify(apartmentRepository, never()).save(any(Apartment.class));
    }

    @Test
    void updateApartmentShouldPersistChanges() {
        ApartmentRequestDTO updated = new ApartmentRequestDTO("Nuevo Nombre", 4L, new BigDecimal("12000.00"));
        ApartmentResponseDTO updatedResponse =
                new ApartmentResponseDTO(apartmentId, "Nuevo Nombre", 4L, new BigDecimal("12000.00"));

        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.findByApartmentNameIgnoreCase("Nuevo Nombre")).thenReturn(Optional.empty());
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(updatedResponse);

        ApartmentResponseDTO result = apartmentService.updateApartment(apartmentId, updated);

        assertEquals(updatedResponse, result);
        verify(apartmentMapper).updateEntity(apartment, updated);
    }

    @Test
    void updateApartmentShouldRejectNameUsedByAnotherApartment() {
        Apartment other = new Apartment(UUID.randomUUID(), "Nuevo Nombre", 2L, PRICE);
        ApartmentRequestDTO updated = new ApartmentRequestDTO("Nuevo Nombre", 4L, PRICE);

        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.findByApartmentNameIgnoreCase("Nuevo Nombre")).thenReturn(Optional.of(other));

        assertThrows(GlobalCustomException.class,
                () -> apartmentService.updateApartment(apartmentId, updated));

        verify(apartmentRepository, never()).save(any(Apartment.class));
    }

    @Test
    void updateApartmentShouldAllowKeepingOwnName() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.findByApartmentNameIgnoreCase("Arrayanes 1")).thenReturn(Optional.of(apartment));
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        assertEquals(response, apartmentService.updateApartment(apartmentId, request));
    }

    @Test
    void updateApartmentShouldFailWhenApartmentDoesNotExist() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> apartmentService.updateApartment(apartmentId, request));
    }

    @Test
    void getApartmentListShouldReturnEmptyListWhenThereAreNoApartments() {
        when(apartmentRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), apartmentService.getApartmentList());
    }

    @Test
    void getApartmentListShouldMapAllApartments() {
        when(apartmentRepository.findAll()).thenReturn(List.of(apartment));
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        assertEquals(List.of(response), apartmentService.getApartmentList());
    }

    @Test
    void findApartmentByIdShouldMapApartment() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        assertEquals(response, apartmentService.findApartmentById(apartmentId));
    }

    @Test
    void findApartmentByIdShouldFailWhenApartmentDoesNotExist() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> apartmentService.findApartmentById(apartmentId));
    }

    @Test
    void editPriceShouldUpdatePrice() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        assertEquals(response, apartmentService.editPrice(apartmentId, new BigDecimal("12000.00")));
        assertEquals(0, new BigDecimal("12000.00").compareTo(apartment.getPrice()));
    }

    @Test
    void editPriceShouldRejectNonPositivePrice() {
        assertThrows(BadRequestException.class, () -> apartmentService.editPrice(apartmentId, BigDecimal.ZERO));
        assertThrows(BadRequestException.class, () -> apartmentService.editPrice(apartmentId, null));
    }

    @Test
    void editApartmentNameShouldUpdateName() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.findByApartmentNameIgnoreCase("Nuevo Nombre")).thenReturn(Optional.empty());
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        apartmentService.editApartName(apartmentId, "Nuevo Nombre");

        assertEquals("Nuevo Nombre", apartment.getApartmentName());
    }

    @Test
    void editApartmentNameShouldRejectBlankName() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

        assertThrows(BadRequestException.class, () -> apartmentService.editApartName(apartmentId, " "));
    }

    @Test
    void editApartmentMaxNumberShouldUpdateCapacity() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(apartmentRepository.save(apartment)).thenReturn(apartment);
        when(apartmentMapper.toResponseDTO(apartment)).thenReturn(response);

        apartmentService.editApartmentMaxNumber(apartmentId, 6L);

        assertEquals(6L, apartment.getApartmentMaxNumber());
    }

    @Test
    void editApartmentMaxNumberShouldRejectNonPositiveValue() {
        assertThrows(BadRequestException.class,
                () -> apartmentService.editApartmentMaxNumber(apartmentId, 0L));
    }

    @Test
    void gettersShouldReadApartmentFields() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));

        assertEquals(4L, apartmentService.getApartmentMaxNumber(apartmentId));
        assertEquals(0, PRICE.compareTo(apartmentService.getPriceById(apartmentId)));
        assertEquals("Arrayanes 1", apartmentService.getApartmentName(apartmentId));
    }

    @Test
    void deleteApartmentShouldDeleteExistingApartmentWithoutBookings() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingRepository.countByApartmentIdAndStatusNot(apartmentId, BookingStatus.CANCELLED))
                .thenReturn(0L);

        apartmentService.deleteApartment(apartmentId);

        verify(apartmentRepository).delete(apartment);
    }

    @Test
    void deleteApartmentShouldFailWithConflictWhenItHasBookings() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingRepository.countByApartmentIdAndStatusNot(apartmentId, BookingStatus.CANCELLED))
                .thenReturn(2L);

        assertThrows(GlobalCustomException.class, () -> apartmentService.deleteApartment(apartmentId));

        verify(apartmentRepository, never()).delete(any());
    }

    @Test
    void deleteApartmentShouldFailWhenApartmentDoesNotExist() {
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> apartmentService.deleteApartment(apartmentId));
    }
}
