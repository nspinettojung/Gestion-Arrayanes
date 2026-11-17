package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.Booking.model.BookingCodeSequence;
import com.Gestion.Arrayanes.Booking.repository.BookingCodeSequenceRepository;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCodeAllocatorTest {

    @Mock
    private BookingCodeSequenceRepository sequenceRepository;
    @Mock
    private BookingRepository bookingRepository;

    private TransactionCodeAllocator allocator;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC);
        allocator = new TransactionCodeAllocator(
                sequenceRepository, bookingRepository, new TransactionCodeGenerator(clock), clock);
    }

    @Test
    void shouldUseLockedSequenceForCurrentYear() {
        BookingCodeSequence sequence = new BookingCodeSequence(1, 2026, 7L);
        when(sequenceRepository.findByIdForUpdate(1)).thenReturn(Optional.of(sequence));

        String code = allocator.nextCode();

        assertEquals("RS-2026-0007", code);
        assertEquals(8L, sequence.getNextValue());
        verify(sequenceRepository).findByIdForUpdate(1);
        verify(sequenceRepository).save(sequence);
        verify(bookingRepository, never()).findAllTransactionCodes();
    }

    @Test
    void shouldResetSequenceWhenYearChanges() {
        BookingCodeSequence sequence = new BookingCodeSequence(1, 2025, 99L);
        when(sequenceRepository.findByIdForUpdate(1)).thenReturn(Optional.of(sequence));
        when(bookingRepository.findAllTransactionCodes()).thenReturn(List.of(
                "RS-2026-0003", "RS-2025-9999", "RS-2026-0007"));

        String code = allocator.nextCode();

        assertEquals("RS-2026-0008", code);
        assertEquals(2026, sequence.getYear());
        assertEquals(9L, sequence.getNextValue());
        verify(sequenceRepository).save(sequence);
    }

    @Test
    void shouldInitializeMissingSequenceRow() {
        when(sequenceRepository.findByIdForUpdate(1)).thenReturn(Optional.empty());
        when(sequenceRepository.saveAndFlush(any(BookingCodeSequence.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String code = allocator.nextCode();

        assertEquals("RS-2026-0001", code);
        ArgumentCaptor<BookingCodeSequence> captor = ArgumentCaptor.forClass(BookingCodeSequence.class);
        verify(sequenceRepository).saveAndFlush(captor.capture());
        assertEquals(1, captor.getValue().getId());
        assertEquals(2026, captor.getValue().getYear());
        assertEquals(2L, captor.getValue().getNextValue());
    }
}
