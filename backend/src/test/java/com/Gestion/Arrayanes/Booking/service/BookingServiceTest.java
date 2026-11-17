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
import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
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
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ApartmentRepository apartmentRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private ClientMapper clientMapper;
    @Mock
    private BookingValidation bookingValidation;
    @Mock
    private TransactionCodeAllocator transactionCodeAllocator;

    private BookingService bookingService;

    private UUID bookingId;
    private UUID clientId;
    private UUID apartmentId;
    private Client client;
    private Apartment apartment;
    private Booking booking;
    private BookingResponseDTO response;

    @BeforeEach
    void setUp() {
        bookingId = UUID.randomUUID();
        clientId = UUID.randomUUID();
        apartmentId = UUID.randomUUID();

        client = new Client(clientId, "Ana", "Perez", "12345678", "555-1234");

        apartment = new Apartment();
        apartment.setId(apartmentId);
        apartment.setApartmentName("Arrayanes 1");
        apartment.setApartmentMaxNumber(4L);
        apartment.setPrice(new BigDecimal("10000.00"));

        booking = new Booking();
        booking.setId(bookingId);
        booking.setArrivalDate(LocalDate.of(2026, 12, 1));
        booking.setNights(3L);
        booking.setDepartureDate(LocalDate.of(2026, 12, 4));
        booking.setAmountPeople(2L);
        booking.setClient(client);
        booking.setApartment(apartment);

        response = new BookingResponseDTO();
        response.setId(bookingId);

        Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        bookingService = new BookingService(bookingRepository, clientRepository, apartmentRepository,
                bookingMapper, clientMapper, bookingValidation, transactionCodeAllocator, clock);
    }

    @Test
    void saveBookingShouldRecalculatePriceAndPersist() {
        BookingWithClientIdRequestDTO request = saveRequest();

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingMapper.toEntity(request, client, apartment)).thenReturn(booking);
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0001");

        BookingResponseDTO result = bookingService.saveBooking(request);

        // (10000 + 1 persona extra * 5000) * 3 noches
        assertEquals(0, new BigDecimal("45000.00").compareTo(booking.getTotalPrice()));
        assertEquals("RS-2026-0001", booking.getTransactionCode());
        assertEquals(BookingStatus.ACTIVE, booking.getStatus());
        assertEquals(response, result);
        verify(bookingValidation).validate(booking, request, apartment);
        verify(bookingRepository).save(booking);
    }

    @Test
    void saveBookingShouldFailWhenClientDoesNotExist() {
        BookingWithClientIdRequestDTO request = saveRequest();
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.saveBooking(request));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void saveBookingShouldFailWhenApartmentDoesNotExist() {
        BookingWithClientIdRequestDTO request = saveRequest();
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.saveBooking(request));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingShouldRecalculatePriceAndPersist() {
        BookingUpdateRequestDTO request = updateRequest();
        booking.setTransactionCode("RS-2026-0005");

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);

        BookingResponseDTO result = bookingService.updateBooking(bookingId, request);

        assertEquals(0, new BigDecimal("45000.00").compareTo(booking.getTotalPrice()));
        assertEquals("RS-2026-0005", booking.getTransactionCode());
        assertEquals(response, result);
        verify(bookingValidation).validate(booking, request, apartment);
        verify(transactionCodeAllocator, never()).nextCode();
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateBookingShouldAssignCodeWhenLegacyBookingHasNone() {
        BookingUpdateRequestDTO request = updateRequest();

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0007");

        bookingService.updateBooking(bookingId, request);

        assertEquals("RS-2026-0007", booking.getTransactionCode());
    }

    @Test
    void updateBookingShouldFailWhenBookingDoesNotExist() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.updateBooking(bookingId, updateRequest()));
    }

    @Test
    void updateBookingShouldRejectStaleVersion() {
        BookingUpdateRequestDTO request = updateRequest();
        request.setVersion(1L);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(OptimisticLockingFailureException.class,
                () -> bookingService.updateBooking(bookingId, request));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void updateBookingShouldRequireVersion() {
        BookingUpdateRequestDTO request = updateRequest();
        request.setVersion(null);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(BadRequestException.class,
                () -> bookingService.updateBooking(bookingId, request));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    @Test
    void deleteBookingShouldFailWhenBookingDoesNotExist() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookingService.deleteBooking(bookingId));
    }

    @Test
    void deleteBookingShouldCancelExistingBookingWithoutRemovingIt() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        bookingService.deleteBooking(bookingId);

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).save(booking);
        verify(bookingRepository, never()).delete(any(Booking.class));
    }

    @Test
    void deleteBookingsShouldRejectEmptyList() {
        assertThrows(BadRequestException.class, () -> bookingService.deleteBookings(List.of()));
        assertThrows(BadRequestException.class, () -> bookingService.deleteBookings(null));
    }

    @Test
    void deleteBookingsShouldFailWhenSomeIdsDoNotExist() {
        UUID missing = UUID.randomUUID();
        when(bookingRepository.findAllById(any())).thenReturn(List.of(booking));

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.deleteBookings(List.of(bookingId, missing)));

        verify(bookingRepository, never()).saveAll(any());
    }

    @Test
    void deleteBookingsShouldCancelAllWhenAllExist() {
        when(bookingRepository.findAllById(any())).thenReturn(List.of(booking));

        bookingService.deleteBookings(List.of(bookingId));

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        verify(bookingRepository).saveAll(List.of(booking));
        verify(bookingRepository, never()).deleteAll(any());
    }

    @Test
    void saveBookingShouldArchiveAnAlreadyFinishedStay() {
        BookingWithClientIdRequestDTO request = saveRequest();
        Stay pastStay = new Stay(LocalDate.of(2026, 11, 1), LocalDate.of(2026, 11, 3), 2L);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingMapper.toEntity(request, client, apartment)).thenReturn(booking);
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(pastStay);
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0001");

        bookingService.saveBooking(request);

        assertEquals(BookingStatus.ARCHIVED, booking.getStatus());
    }

    @Test
    void cancelledBookingShouldNotBeVisibleById() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        assertThrows(ResourceNotFoundException.class, () -> bookingService.findBookingById(bookingId));
    }

    @Test
    void createBookingWithClientShouldReuseExistingClient() {
        BookingWithNewClientRequestDTO request = createWithClientRequest();
        when(clientRepository.findByClientDni("12345678")).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingMapper.toEntity(request, client, apartment)).thenReturn(booking);
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0002");

        BookingResponseDTO result = bookingService.createBookingWithClient(request);

        assertEquals(response, result);
        assertEquals(0, new BigDecimal("45000.00").compareTo(booking.getTotalPrice()));
        assertEquals("RS-2026-0002", booking.getTransactionCode());
        verify(clientRepository, never()).save(any(Client.class));
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBookingWithClientShouldCreateNewClientWhenDniDoesNotExist() {
        BookingWithNewClientRequestDTO request = createWithClientRequest();
        when(clientRepository.findByClientDni("12345678")).thenReturn(Optional.empty());
        when(clientMapper.toEntity(request.getClient())).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingMapper.toEntity(request, client, apartment)).thenReturn(booking);
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0003");

        BookingResponseDTO result = bookingService.createBookingWithClient(request);

        assertEquals(response, result);
        assertEquals("RS-2026-0003", booking.getTransactionCode());
        verify(clientRepository).save(client);
        verify(bookingRepository).save(booking);
    }

    @Test
    void createBookingWithClientShouldNormalizeDniWhenLookingUpClient() {
        BookingWithNewClientRequestDTO request = createWithClientRequest();
        request.getClient().setClientDni(" ab-123 ");
        when(clientRepository.findByClientDni("AB-123")).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.of(apartment));
        when(bookingMapper.toEntity(request, client, apartment)).thenReturn(booking);
        when(bookingValidation.validate(booking, request, apartment)).thenReturn(stay());
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);
        when(transactionCodeAllocator.nextCode()).thenReturn("RS-2026-0008");

        bookingService.createBookingWithClient(request);

        verify(clientRepository).findByClientDni("AB-123");
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void createBookingWithClientShouldRejectMissingClientData() {
        BookingWithNewClientRequestDTO request = createWithClientRequest();
        request.setClient(null);

        assertThrows(BadRequestException.class,
                () -> bookingService.createBookingWithClient(request));
    }

    @Test
    void createBookingWithClientShouldFailWhenApartmentDoesNotExist() {
        BookingWithNewClientRequestDTO request = createWithClientRequest();
        when(clientRepository.findByClientDni("12345678")).thenReturn(Optional.of(client));
        when(apartmentRepository.findById(apartmentId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> bookingService.createBookingWithClient(request));
    }

    @Test
    void getListBookingShouldMapAllBookings() {
        when(bookingRepository.findAllByStatusNotOrderByArrivalDateAsc(BookingStatus.CANCELLED))
                .thenReturn(List.of(booking));
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);

        assertEquals(List.of(response), bookingService.getListBooking());
    }

    @Test
    void findBookingByIdShouldMapBooking() {
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        when(bookingMapper.toResponseDTO(booking)).thenReturn(response);

        assertEquals(response, bookingService.findBookingById(bookingId));
    }

    private Stay stay() {
        return new Stay(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4), 3L);
    }

    private BookingWithClientIdRequestDTO saveRequest() {
        BookingWithClientIdRequestDTO request = new BookingWithClientIdRequestDTO();
        fillCommonFields(request);
        request.setClientId(clientId);
        return request;
    }

    private BookingUpdateRequestDTO updateRequest() {
        BookingUpdateRequestDTO request = new BookingUpdateRequestDTO();
        fillCommonFields(request);
        request.setClientId(clientId);
        request.setVersion(0L);
        return request;
    }

    private BookingWithNewClientRequestDTO createWithClientRequest() {
        BookingWithNewClientRequestDTO request = new BookingWithNewClientRequestDTO();
        fillCommonFields(request);
        request.setClient(new ClientRequestDTO("Ana", "Perez", "12345678", "555-1234"));
        return request;
    }

    private void fillCommonFields(BookingBaseRequestDTO request) {
        request.setArrivalDate(LocalDate.of(2026, 12, 1));
        request.setNights(3L);
        request.setApartmentId(apartmentId);
        request.setAmountPeople(2L);
    }
}
