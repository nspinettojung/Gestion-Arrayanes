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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ApartmentService implements IApartmentService {

    private final ApartmentRepository apartmentRepository;
    private final ApartmentMapper apartmentMapper;
    private final BookingRepository bookingRepository;

    public ApartmentService(ApartmentRepository apartmentRepository, ApartmentMapper apartmentMapper,
                            BookingRepository bookingRepository) {
        this.apartmentRepository = apartmentRepository;
        this.apartmentMapper = apartmentMapper;
        this.bookingRepository = bookingRepository;
    }

    private Apartment getApartmentById(UUID id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("No se encontró el apartamento con id: " + id));
    }

    private void validateApartmentNameAvailable(String apartmentName, UUID currentApartmentId) {
        if (apartmentName == null || apartmentName.isBlank()) {
            throw new BadRequestException("El nombre del apartamento es obligatorio.");
        }

        apartmentRepository.findByApartmentNameIgnoreCase(apartmentName.trim())
                .filter(apartment -> !apartment.getId().equals(currentApartmentId))
                .ifPresent(apartment -> {
                    throw new GlobalCustomException("Ya existe un apartamento con ese nombre.");
                });
    }

    @Override
    @Transactional
    public ApartmentResponseDTO saveApartment(ApartmentRequestDTO apartmentRequestDTO) {
        validateApartmentNameAvailable(apartmentRequestDTO.getApartmentName(), null);

        Apartment apartment = apartmentMapper.toEntity(apartmentRequestDTO);
        Apartment saved = apartmentRepository.save(apartment);

        return apartmentMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteApartment(UUID id) {
        Apartment apartment = getApartmentById(id);

        long bookings = bookingRepository.countByApartmentIdAndStatusNot(id, BookingStatus.CANCELLED);
        if (bookings > 0) {
            throw new GlobalCustomException(
                    "No se puede eliminar el departamento porque tiene reservas asociadas.");
        }

        apartmentRepository.delete(apartment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApartmentResponseDTO> getApartmentList() {
        return apartmentRepository.findAll().stream()
                .map(apartmentMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ApartmentResponseDTO findApartmentById(UUID id) {
        return apartmentMapper.toResponseDTO(getApartmentById(id));
    }

    @Override
    @Transactional
    public ApartmentResponseDTO updateApartment(UUID id, ApartmentRequestDTO apartmentRequestDTO) {
        Apartment apartment = getApartmentById(id);
        validateApartmentNameAvailable(apartmentRequestDTO.getApartmentName(), apartment.getId());

        apartmentMapper.updateEntity(apartment, apartmentRequestDTO);
        Apartment updateApartment = apartmentRepository.save(apartment);

        return apartmentMapper.toResponseDTO(updateApartment);
    }

    @Override
    @Transactional(readOnly = true)
    public Long getApartmentMaxNumber(UUID id) {
        return getApartmentById(id).getApartmentMaxNumber();
    }

    @Override
    @Transactional
    public ApartmentResponseDTO editApartmentMaxNumber(UUID id, Long apartNumber) {
        if (apartNumber == null || apartNumber <= 0) {
            throw new BadRequestException("La capacidad del apartamento debe ser mayor a cero.");
        }

        Apartment apartment = getApartmentById(id);
        apartment.setApartmentMaxNumber(apartNumber);

        return apartmentMapper.toResponseDTO(apartmentRepository.save(apartment));
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getPriceById(UUID id) {
        return getApartmentById(id).getPrice();
    }

    @Override
    @Transactional
    public ApartmentResponseDTO editPrice(UUID id, BigDecimal price) {
        if (price == null || price.signum() <= 0) {
            throw new BadRequestException("El precio del apartamento debe ser mayor a cero.");
        }

        Apartment apartment = getApartmentById(id);
        apartment.setPrice(price);

        return apartmentMapper.toResponseDTO(apartmentRepository.save(apartment));
    }

    @Override
    @Transactional(readOnly = true)
    public String getApartmentName(UUID id) {
        return getApartmentById(id).getApartmentName();
    }

    @Override
    @Transactional
    public ApartmentResponseDTO editApartName(UUID id, String apartName) {
        Apartment apartment = getApartmentById(id);
        validateApartmentNameAvailable(apartName, apartment.getId());

        apartment.setApartmentName(apartName.trim());

        return apartmentMapper.toResponseDTO(apartmentRepository.save(apartment));
    }
}
