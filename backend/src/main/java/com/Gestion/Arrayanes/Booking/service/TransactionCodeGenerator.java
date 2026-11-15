package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Collection;

@Component
public class TransactionCodeGenerator {

    private static final String PREFIX = "RS";

    private final Clock clock;

    public TransactionCodeGenerator(Clock clock) {
        this.clock = clock;
    }

    public String generate(Long sequence) {
        if (sequence == null || sequence <= 0) {
            throw new BadRequestException("La secuencia del código de transacción debe ser mayor a cero.");
        }

        return currentPrefix() + String.format("%04d", sequence);
    }

    /**
     * Calcula el siguiente código a partir de los códigos ya persistidos del año en curso.
     * Ignora códigos de otros años o con formato inválido.
     */
    public String generateNext(Collection<String> existingCodes) {
        String prefix = currentPrefix();

        long maxSequence = existingCodes == null ? 0L : existingCodes.stream()
                .filter(code -> code != null && code.startsWith(prefix))
                .map(code -> code.substring(prefix.length()))
                .filter(sequence -> sequence.matches("\\d+"))
                .mapToLong(Long::parseLong)
                .max()
                .orElse(0L);

        return generate(maxSequence + 1);
    }

    public String currentPrefix() {
        int year = LocalDate.now(clock).getYear();
        return String.format("%s-%d-", PREFIX, year);
    }
}
