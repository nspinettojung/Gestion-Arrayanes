package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransactionCodeGeneratorTest {

    private TransactionCodeGenerator generator;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        generator = new TransactionCodeGenerator(fixedClock);
    }

    @Test
    void shouldGenerateCodeWithYearAndPaddedSequence() {
        assertEquals("RS-2026-0001", generator.generate(1L));
        assertEquals("RS-2026-0042", generator.generate(42L));
    }

    @Test
    void shouldNotTruncateSequencesLongerThanFourDigits() {
        assertEquals("RS-2026-12345", generator.generate(12345L));
    }

    @Test
    void shouldGenerateFirstCodeWhenThereAreNoPreviousCodes() {
        assertEquals("RS-2026-0001", generator.generateNext(List.of()));
        assertEquals("RS-2026-0001", generator.generateNext(null));
    }

    @Test
    void shouldGenerateNextSequenceFromPersistedCodes() {
        List<String> existingCodes = Arrays.asList(
                "RS-2026-0003",
                "RS-2026-0007",
                "RS-2025-0099",
                "RS-2026-XXXX",
                null
        );

        assertEquals("RS-2026-0008", generator.generateNext(existingCodes));
    }

    @Test
    void shouldNotTruncateNextSequenceLongerThanFourDigits() {
        assertEquals("RS-2026-12346", generator.generateNext(List.of("RS-2026-12345")));
    }

    @Test
    void shouldRejectNullSequence() {
        assertThrows(BadRequestException.class, () -> generator.generate(null));
    }

    @Test
    void shouldRejectNonPositiveSequence() {
        assertThrows(BadRequestException.class, () -> generator.generate(0L));
        assertThrows(BadRequestException.class, () -> generator.generate(-5L));
    }
}
