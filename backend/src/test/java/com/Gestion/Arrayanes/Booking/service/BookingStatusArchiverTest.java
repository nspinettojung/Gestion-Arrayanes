package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingStatusArchiverTest {

    @Mock
    private BookingRepository bookingRepository;

    private BookingStatusArchiver archiver;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        archiver = new BookingStatusArchiver(bookingRepository, clock);
    }

    @Test
    void shouldArchiveBookingsFinishedBeforeToday() {
        when(bookingRepository.archiveExpiredBookings(
                LocalDate.of(2026, 11, 14), BookingStatus.ACTIVE, BookingStatus.ARCHIVED))
                .thenReturn(3);

        assertEquals(3, archiver.archiveExpiredBookings());

        verify(bookingRepository).archiveExpiredBookings(
                LocalDate.of(2026, 11, 14), BookingStatus.ACTIVE, BookingStatus.ARCHIVED);
    }
}
