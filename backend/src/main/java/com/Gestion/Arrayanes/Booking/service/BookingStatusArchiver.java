package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/** Pasa a ARCHIVED las reservas cuya estadía ya terminó. */
@Component
public class BookingStatusArchiver {

    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingStatusArchiver(BookingRepository bookingRepository, Clock clock) {
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    @Scheduled(cron = "0 0 1 * * *")
    @Transactional
    public int archiveExpiredBookings() {
        return bookingRepository.archiveExpiredBookings(
                LocalDate.now(clock), BookingStatus.ACTIVE, BookingStatus.ARCHIVED);
    }
}
