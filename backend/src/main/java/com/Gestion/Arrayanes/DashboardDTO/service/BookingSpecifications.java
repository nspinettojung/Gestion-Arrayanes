package com.Gestion.Arrayanes.DashboardDTO.service;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class BookingSpecifications {

    public static Specification<Booking> hasClientLastName(String lastName) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("client").get("clientLastName")),
                "%" + lastName.toLowerCase() + "%"
        );
    }

    public static Specification<Booking> betweenDates(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.between(root.get("arrivalDate"), start, end);
    }

    public static Specification<Booking> apartmentEquals(UUID apartmentId) {
        return (root, query, cb) -> cb.equal(root.get("apartment").get("id"), apartmentId);
    }

    public static Specification<Booking> departureOnOrAfter(LocalDate date) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("departureDate"), date);
    }

    public static Specification<Booking> statusNot(BookingStatus status) {
        return (root, query, cb) -> cb.notEqual(root.get("status"), status);
    }

    public static Specification<Booking> statusEquals(BookingStatus status) {
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }
}
