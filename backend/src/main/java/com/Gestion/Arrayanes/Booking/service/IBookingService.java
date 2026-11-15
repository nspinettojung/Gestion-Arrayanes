package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.dto.BookingRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IBookingService {

    BookingResponseDTO saveBooking(BookingRequestDTO bookingRequestDTO);

    BookingResponseDTO createBookingWithClient(BookingRequestDTO bookingRequestDTO);

    void deleteBooking(UUID id);

    void deleteBookings(List<UUID> ids);

    List<BookingResponseDTO> getListBooking();

    BookingResponseDTO findBookingById(UUID id);

    BookingResponseDTO updateBooking(UUID id, BookingRequestDTO bookingRequestDTO);

    String transactionCodeGenerator();
}
