package com.Gestion.Arrayanes.Booking.repository;

import com.Gestion.Arrayanes.Booking.model.BookingCodeSequence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ImportAutoConfiguration(FlywayAutoConfiguration.class)
class BookingCodeSequenceRepositoryTest {

    @Autowired
    private BookingCodeSequenceRepository repository;

    @Test
    void shouldFindSingletonSequenceCreatedByMigration() {
        Optional<BookingCodeSequence> sequence = repository.findByIdForUpdate(1);

        assertTrue(sequence.isPresent());
        assertEquals(1, sequence.get().getId());
        assertEquals(0, sequence.get().getYear());
        assertEquals(1L, sequence.get().getNextValue());
    }
}
