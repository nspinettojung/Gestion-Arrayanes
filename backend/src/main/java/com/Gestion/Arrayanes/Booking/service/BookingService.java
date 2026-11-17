package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Apartment.repository.ApartmentRepository;
import com.Gestion.Arrayanes.Booking.dto.BookingBaseRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingUpdateRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithNewClientRequestDTO;
import com.Gestion.Arrayanes.Booking.mapper.BookingMapper;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.mapper.ClientMapper;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.repository.ClientRepository;
import com.Gestion.Arrayanes.Clients.util.ClientNormalization;
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
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
    private final TransactionCodeAllocator transactionCodeAllocator;
    private final Clock clock;

    public BookingService(BookingRepository bookingRepository,
                          ClientRepository clientRepository,
                          ApartmentRepository apartmentRepository,
                          BookingMapper bookingMapper,
                          ClientMapper clientMapper,
                          BookingValidation bookingValidation,
                          TransactionCodeAllocator transactionCodeAllocator,
                          Clock clock) {
        this.bookingRepository = bookingRepository;
        this.clientRepository = clientRepository;
        this.apartmentRepository = apartmentRepository;
        this.bookingMapper = bookingMapper;
        this.clientMapper = clientMapper;
        this.bookingValidation = bookingValidation;
        this.transactionCodeAllocator = transactionCodeAllocator;
        this.clock = clock;
    }

    private Booking getBookingById(UUID id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reserva no encontrada."));
    }

    /** Las reservas canceladas se ocultan como si no existieran. */
    private Booking getVisibleBookingById(UUID id) {
        Booking booking = getBookingById(id);

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new ResourceNotFoundException("Reserva no encontrada.");
        }

        return booking;
    }

    /** Una reserva pasada queda como historial; una futura vuelve a estar activa. */
    private void applyStatusFromDates(Booking booking) {
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return;
        }

        LocalDate today = LocalDate.now(clock);
        boolean expired = booking.getDepartureDate() != null && booking.getDepartureDate().isBefore(today);
        booking.setStatus(expired ? BookingStatus.ARCHIVED : BookingStatus.ACTIVE);
    }

    private Client getClientById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente no encontrado."));
    }

    private Apartment getApartmentById(UUID id) {
        return apartmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Apartamento no encontrado."));
    }

    private void calculateAndSetTotalPrice(Booking booking, Apartment apartment) {
        BigDecimal totalPrice = BookingPrice.calculatePrice(
                apartment.getPrice(),
                booking.getAmountPeople(),
                booking.getNights()
        );
        booking.setTotalPrice(totalPrice);
    }

    /**
     * Comprueba que el cliente haya leído la última versión de la reserva.
     * La validación de {@code @Version} de JPA sigue protegiendo la carrera
     * entre esta comprobación y el commit de la transacción.
     */
    private void validateVersion(Booking booking, BookingUpdateRequestDTO bookingRequestDTO) {
        Long requestedVersion = bookingRequestDTO.getVersion();

        if (requestedVersion == null) {
            throw new BadRequestException("La versión de la reserva es obligatoria para editarla.");
        }

        if (booking.getVersion() != requestedVersion) {
            throw new OptimisticLockingFailureException(
                    "La reserva fue modificada por otro usuario (versión esperada: "
                            + requestedVersion + ", versión actual: " + booking.getVersion() + ").");
        }
    }

    private String nextTransactionCode() {
        return transactionCodeAllocator.nextCode();
    }

    private BookingResponseDTO createBooking(BookingBaseRequestDTO request, Client client) {
        Apartment apartment = getApartmentById(request.getApartmentId());
        Booking booking = bookingMapper.toEntity(request, client, apartment);

        Stay stay = bookingValidation.validate(booking, request, apartment);
        stay.applyTo(booking);
        applyStatusFromDates(booking);
        calculateAndSetTotalPrice(booking, apartment);
        booking.setTransactionCode(nextTransactionCode());
        bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO saveBooking(BookingWithClientIdRequestDTO bookingRequestDTO) {
        return createBooking(bookingRequestDTO, getClientById(bookingRequestDTO.getClientId()));
    }

    @Override
    @Transactional
    public void deleteBooking(UUID id) {
        Booking booking = getBookingById(id);
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
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

        bookings.forEach(booking -> booking.setStatus(BookingStatus.CANCELLED));
        bookingRepository.saveAll(bookings);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookingResponseDTO> getListBooking() {
        List<Booking> bookings = bookingRepository
                .findAllByStatusNotOrderByArrivalDateAsc(BookingStatus.CANCELLED);

        return bookings.stream()
                .map(bookingMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BookingResponseDTO findBookingById(UUID id) {
        Booking booking = getVisibleBookingById(id);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO updateBooking(UUID id, BookingUpdateRequestDTO bookingRequestDTO) {
        Booking booking = getVisibleBookingById(id);
        validateVersion(booking, bookingRequestDTO);
        Client client = getClientById(bookingRequestDTO.getClientId());
        Apartment apartment = getApartmentById(bookingRequestDTO.getApartmentId());

        bookingMapper.bookingUpdate(booking, bookingRequestDTO, client, apartment);
        Stay stay = bookingValidation.validate(booking, bookingRequestDTO, apartment);
        stay.applyTo(booking);
        applyStatusFromDates(booking);
        calculateAndSetTotalPrice(booking, apartment);
        if (booking.getTransactionCode() == null || booking.getTransactionCode().isBlank()) {
            booking.setTransactionCode(nextTransactionCode());
        }
        bookingRepository.save(booking);

        return bookingMapper.toResponseDTO(booking);
    }

    @Override
    @Transactional
    public BookingResponseDTO createBookingWithClient(BookingWithNewClientRequestDTO request) {
        ClientRequestDTO clientDto = request.getClient();

        if (clientDto == null) {
            throw new BadRequestException("Debe indicar los datos del cliente.");
        }

        Client client = clientRepository.findByClientDni(
                        ClientNormalization.normalizeDni(clientDto.getClientDni()))
                .orElseGet(() -> clientRepository.save(clientMapper.toEntity(clientDto)));

        return createBooking(request, client);
    }
}
