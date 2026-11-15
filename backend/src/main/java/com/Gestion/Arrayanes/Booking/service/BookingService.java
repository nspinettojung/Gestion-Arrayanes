package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.dto.BookingRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.mapper.BookingMapper;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.mapper.ClientMapper;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.repository.ClientRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
public class BookingService implements IBookingService {

    private final BookingRepository bookingRepository;
    private final ClientRepository clientRepository;
    private final ApartmentRepository apartmentRepository;
    private final BookingMapper bookingMapper;
    private final ClientMapper clientMapper;
    private final BookingValidation bookingValidation;
    private final TransactionCodeGenerator transactionCodeGenerator;

    public BookingService(BookingRepository bookingRepository,
                          ClientRepository clientRepository,
                          ApartmentRepository apartmentRepository,
                          BookingMapper bookingMapper,
                          ClientMapper clientMapper,
                          BookingValidation bookingValidation,
                          TransactionCodeGenerator transactionCodeGenerator) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.apartmentRepository = apartmentRepository;
        this.bookingMapper = bookingMapper;
        this.clientMapper = clientMapper;
        this.bookingValidation = bookingValidation;
        this.transactionCodeGenerator = transactionCodeGenerator;
    }

    private Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));
    }

    private Client getClientById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
    }

    private Apartment getApartmentById(UUID id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartamento no encontrado."));
    }

    private void saveBookingEntity(Booking booking) {
        bookingRepository.save(booking);
    }

    private void calculateAndSetTotalPrice(Booking booking, Apartment apartment) {
        BigDecimal totalPrice = BookingPrice.calculatePrice(
                apartment.getPrice(),
                booking.getAmountPeople(),
                booking.getNights()
        );
        booking.setTotalPrice(totalPrice);
    }

    @Override
    public String transactionCodeGenerator() {
        return nextTransactionCode();
    }

    private String nextTransactionCode() {
        return transactionCodeGenerator.generateNext(bookingRepository.findAllTransactionCodes());
    }

    @Override
    @Transactional
    public BookingResponseDTO saveBooking(BookingRequestDTO bookingRequestDTO) {
        Client client = getClientById(bookingRequestDTO.getClientId());
        Apartment apartment = getApartmentById(bookingRequestDTO.getApartmentId());
        Booking booking = bookingMapper.toEntity(bookingRequestDTO, client, apartment);

        bookingValidation.validate(booking, bookingRequestDTO, apartment);
        calculateAndSetTotalPrice(booking, apartment);
        booking.setTransactionCode(nextTransactionCode());
        saveBookingEntity(booking);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(UUID id) {
        Booking booking = getBookingById(id);
        bookingRepository.delete(booking);
    }

    @Override
    @Transactional
    public void deleteBookings(List<UUID> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException("No se seleccionaron reservas.");
        }

        Set<UUID> uniqueIds = new HashSet<>(ids);

        if (uniqueIds.contains(null)) {
            throw new BadRequestException("La lista de reservas contiene un identificador inválido.");
        }

        List<Booking> bookings = bookingRepository.findAllById(uniqueIds);

        if (bookings.size() != uniqueIds.size()) {
            throw new ResourceNotFoundException("Una o más reservas no existen.");
        }

        bookingRepository.deleteAll(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getListBooking() {
        List<Booking> bookings = bookingRepository.findAll();

        return bookings.stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO findBookingById(UUID id) {
        Booking booking = getBookingById(id);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBooking(UUID id, BookingRequestDTO bookingRequestDTO) {
        Booking booking = getBookingById(id);
        Client client = getClientById(bookingRequestDTO.getClientId());
        Apartment apartment = getApartmentById(bookingRequestDTO.getApartmentId());

        bookingMapper.bookingUpdate(booking, bookingRequestDTO, client, apartment);
        bookingValidation.validate(booking, bookingRequestDTO, apartment);
        calculateAndSetTotalPrice(booking, apartment);
        if (booking.getTransactionCode() == null || booking.getTransactionCode().isBlank()) {
            booking.setTransactionCode(nextTransactionCode());
        }
        saveBookingEntity(booking);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO createBookingWithClient(BookingRequestDTO request) {
        ClientRequestDTO clientDto = request.getClient();

        if (clientDto == null) {
            throw new BadRequestException("Debe indicar los datos del cliente.");
        }

        Client client = clientRepository.findByClientDni(clientDto.getClientDni())
                .orElseGet(() -> clientRepository.save(clientMapper.toEntity(clientDto)));

        Apartment apartment = getApartmentById(request.getApartmentId());

        Booking booking = bookingMapper.toEntity(request, client, apartment);
        bookingValidation.validate(booking, request, apartment);
        calculateAndSetTotalPrice(booking, apartment);
        booking.setTransactionCode(nextTransactionCode());

        bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(booking);
    }
}
