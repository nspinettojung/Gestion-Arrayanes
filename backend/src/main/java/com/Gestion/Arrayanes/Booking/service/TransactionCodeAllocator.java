package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.model.BookingCodeSequence;
import com.Gestion.Arrayanes.Booking.repository.BookingCodeSequenceRepository;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Objects;

/** Asigna códigos de transacción de forma segura usando la fila de secuencia bloqueada. */
@Component
public class TransactionCodeAllocator {

    private static final int SEQUENCE_ID = 1;

    private final BookingCodeSequenceRepository sequenceRepository;
    private final BookingRepository bookingRepository;
    private final TransactionCodeGenerator generator;
    private final Clock clock;

    public TransactionCodeAllocator(BookingCodeSequenceRepository sequenceRepository,
                                    BookingRepository bookingRepository,
                                    TransactionCodeGenerator generator,
                                    Clock clock) {
        this.sequenceRepository = sequenceRepository;
        this.bookingRepository = bookingRepository;
        this.generator = generator;
        this.clock = clock;
    }

    @Transactional
    public String nextCode() {
        BookingCodeSequence sequence = sequenceRepository.findByIdForUpdate(SEQUENCE_ID)
                .orElseGet(() -> sequenceRepository.saveAndFlush(
                        new BookingCodeSequence(SEQUENCE_ID, 0, 1L)));

        int year = LocalDate.now(clock).getYear();
        long next;

        if (!Objects.equals(sequence.getYear(), year)) {
            next = generator.nextSequence(bookingRepository.findAllTransactionCodes());
            sequence.setYear(year);
        } else {
            next = sequence.getNextValue();
        }

        sequence.setNextValue(next + 1);
        sequenceRepository.save(sequence);

        return generator.generate(next);
    }
}
