package com.Gestion.Arrayanes.Booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Gestion.Arrayanes.Clients.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.Gestion.Arrayanes.Booking.model.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    // Códigos de transacción ya asignados (para calcular el siguiente de forma estable)
    @Query("SELECT b.transactionCode FROM Booking b WHERE b.transactionCode IS NOT NULL")
    List<String> findAllTransactionCodes();

    // Contar reservas que llegan en una fecha específica
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.arrivalDate = :date")
    long countBookingsByArrivalDate(@Param("date") LocalDate date);

    // Contar reservas activas en una fecha (llegaron y todavía no se fueron)
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.arrivalDate <= :date AND b.departureDate > :date")
    long countOccupiedBookingsOnDate(@Param("date") LocalDate date);

    // Contar reservas que salen en una fecha específica
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.departureDate = :date")
    long countDeparturesByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.arrivalDate BETWEEN :start AND :end")
    long countBookingsByArrivalDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.departureDate BETWEEN :start AND :end")
    long countBookingsByDepartureDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);


    @Query("SELECT b FROM Booking b " +
            "WHERE b.apartmentId.id = :apartmentId " +
            "AND (:bookingId IS NULL OR b.id <> :bookingId) " +
            "AND b.arrivalDate < :departureDate " +
            "AND b.departureDate > :arrivalDate")
    List<Booking> findOverlappingBookings(UUID apartmentId,
                                          UUID bookingId,
                                          LocalDate arrivalDate,
                                          LocalDate departureDate);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.clientId c " +
            "JOIN FETCH b.apartmentId a " +
            "WHERE b.arrivalDate BETWEEN :start AND :end")
    List<Booking> findArrivalsInMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);


    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.clientId c " +
            "JOIN FETCH b.apartmentId a " +
            "WHERE b.departureDate BETWEEN :start AND :end")
    List<Booking> findDeparturesInMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);


    // Buscar reservas que llegan en una fecha específica
    @Query("SELECT b FROM Booking b JOIN FETCH b.clientId c JOIN FETCH b.apartmentId a WHERE b.arrivalDate = :date")
    List<Booking> findArrivalsByDate(@Param("date") LocalDate date);

    // Buscar reservas que salen en una fecha específica
    @Query("SELECT b FROM Booking b JOIN FETCH b.clientId c JOIN FETCH b.apartmentId a WHERE b.departureDate = :date")
    List<Booking> findDeparturesByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.arrivalDate <= :end AND b.departureDate > :start")
    List<Booking> findBookingsInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Booking> findAllByOrderByArrivalDateAsc();

    Optional<Booking> findTopByClientIdOrderByArrivalDateDesc(Client client);

}
