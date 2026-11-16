package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingUpdateRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithNewClientRequestDTO;

import java.util.List;
import java.util.UUID;

public interface IBookingService {

    BookingResponseDTO saveBooking(BookingWithClientIdRequestDTO bookingRequestDTO);

    BookingResponseDTO createBookingWithClient(BookingWithNewClientRequestDTO bookingRequestDTO);

    void deleteBooking(UUID id);

    void deleteBookings(List<UUID> ids);

    List<BookingResponseDTO> getListBooking();

    BookingResponseDTO findBookingById(UUID id);

    BookingResponseDTO updateBooking(UUID id, BookingUpdateRequestDTO bookingRequestDTO);

    String transactionCodeGenerator();
}
