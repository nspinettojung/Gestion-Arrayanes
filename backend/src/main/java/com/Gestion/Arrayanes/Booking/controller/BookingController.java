package com.Gestion.Arrayanes.Booking.controller;

import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingUpdateRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithClientIdRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingWithNewClientRequestDTO;
import com.Gestion.Arrayanes.Booking.service.IBookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final IBookingService iBookingService;
    private final Clock clock;

    public BookingController(IBookingService iBookingService, Clock clock) {
        this.iBookingService = iBookingService;
        this.clock = clock;
    }

    @PostMapping("/createWithClient")
    public ResponseEntity<BookingResponseDTO> createBookingWithClient(
            @Valid @RequestBody BookingWithNewClientRequestDTO request) {
        BookingResponseDTO response = iBookingService.createBookingWithClient(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/createbooking")
    public ResponseEntity<BookingResponseDTO> createBooking(
            @Valid @RequestBody BookingWithClientIdRequestDTO requestBooking) {
        BookingResponseDTO booking = iBookingService.saveBooking(requestBooking);
        URI location = URI.create("/api/bookings/createbooking/" + booking.getId());
        return ResponseEntity.created(location).body(booking);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable UUID id) {
        iBookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteBookingList(@RequestBody List<UUID> ids) {
        iBookingService.deleteBookings(ids);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookinglist")
    public ResponseEntity<List<BookingResponseDTO>> getBookingList() {
        return ResponseEntity.ok(iBookingService.getListBooking());
    }

    @PutMapping("/editbooking/{id}")
    public ResponseEntity<BookingResponseDTO> updateBooking(@PathVariable UUID id,
                                                            @Valid @RequestBody BookingUpdateRequestDTO bookingRequestDTO) {
        return ResponseEntity.ok(iBookingService.updateBooking(id, bookingRequestDTO));
    }

    @GetMapping("/currentdate")
    public ResponseEntity<LocalDate> getCurrentDate() {
        LocalDate today = LocalDate.now(clock);
        return ResponseEntity.ok(today);
    }

}
