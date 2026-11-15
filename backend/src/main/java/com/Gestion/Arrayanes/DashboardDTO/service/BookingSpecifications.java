package com.Gestion.Arrayanes.DashboardDTO.service;

import com.Gestion.Arrayanes.Booking.model.Booking;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.UUID;

public class BookingSpecifications {

    public static Specification<Booking> hasClientLastName(String lastName) {
        return (root, query, cb) -> cb.like(
                cb.lower(root.get("clientId").get("clientLastName")),
                "%" + lastName.toLowerCase() + "%"
        );
    }

    public static Specification<Booking> betweenDates(LocalDate start, LocalDate end) {
        return (root, query, cb) -> cb.between(root.get("arrivalDate"), start, end);
    }

    public static Specification<Booking> apartmentEquals(UUID apartmentId) {
        return (root, query, cb) -> cb.equal(root.get("apartmentId").get("id"), apartmentId);
    }
}
