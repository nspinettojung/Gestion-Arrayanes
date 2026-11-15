package com.Gestion.Arrayanes.Booking.service;

import com.Gestion.Arrayanes.GlobalHandlerException.BadRequestException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class BookingPriceTest {

    private static final BigDecimal BASE_PRICE = new BigDecimal("10000.00");

    @Test
    void shouldChargeOnlyBasePriceForOnePersonOneNight() {
        BigDecimal total = BookingPrice.calculatePrice(BASE_PRICE, 1L, 1L);

        assertAmount("10000.00", total);
    }

    @Test
    void shouldChargeExtraPersonFee() {
        BigDecimal total = BookingPrice.calculatePrice(BASE_PRICE, 2L, 1L);

        assertAmount("15000.00", total);
    }

    @Test
    void shouldMultiplyByNights() {
        BigDecimal total = BookingPrice.calculatePrice(BASE_PRICE, 4L, 3L);

        // (10000 + 3 personas extra * 5000) * 3 noches
        assertAmount("75000.00", total);
    }

    @Test
    void shouldRejectNullParameters() {
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(null, 1L, 1L));
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(BASE_PRICE, null, 1L));
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(BASE_PRICE, 1L, null));
    }

    @Test
    void shouldRejectNonPositivePeopleOrNights() {
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(BASE_PRICE, 0L, 1L));
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(BASE_PRICE, 1L, 0L));
        assertThrows(BadRequestException.class, () -> BookingPrice.calculatePrice(BASE_PRICE, -1L, 1L));
    }

    @Test
    void shouldRejectNegativeApartmentPrice() {
        assertThrows(BadRequestException.class,
                () -> BookingPrice.calculatePrice(new BigDecimal("-1"), 1L, 1L));
    }

    private void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, new BigDecimal(expected).compareTo(actual),
                () -> "Esperado " + expected + " pero fue " + actual);
    }
}
