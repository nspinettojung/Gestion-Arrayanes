package com.Gestion.Arrayanes.Booking.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, UUID>, JpaSpecificationExecutor<Booking> {

    String ACTIVE = "com.Gestion.Arrayanes.Booking.model.BookingStatus.ACTIVE";

    // Los códigos se conservan aunque la reserva se cancele o se archive: nunca se reutilizan.
    @Query("SELECT b.transactionCode FROM Booking b WHERE b.transactionCode IS NOT NULL")
    List<String> findAllTransactionCodes();

    // Solo reservas activas: canceladas no existen y archivadas son historial.
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = " + ACTIVE + " AND b.arrivalDate = :date")
    long countBookingsByArrivalDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = " + ACTIVE
            + " AND b.arrivalDate <= :date AND b.departureDate > :date")
    long countOccupiedBookingsOnDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = " + ACTIVE + " AND b.departureDate = :date")
    long countDeparturesByDate(@Param("date") LocalDate date);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = " + ACTIVE
            + " AND b.arrivalDate BETWEEN :start AND :end")
    long countBookingsByArrivalDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = " + ACTIVE
            + " AND b.departureDate BETWEEN :start AND :end")
    long countBookingsByDepartureDateBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Booking b " +
            "WHERE b.status = " + ACTIVE + " " +
            "AND b.apartment.id = :apartmentId " +
            "AND (:bookingId IS NULL OR b.id <> :bookingId) " +
            "AND b.arrivalDate < :departureDate " +
            "AND b.departureDate > :arrivalDate")
    List<Booking> findOverlappingBookings(UUID apartmentId,
                                          UUID bookingId,
                                          LocalDate arrivalDate,
                                          LocalDate departureDate);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.client c " +
            "JOIN FETCH b.apartment a " +
            "WHERE b.status = " + ACTIVE + " AND b.arrivalDate BETWEEN :start AND :end")
    List<Booking> findArrivalsInMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Booking b " +
            "JOIN FETCH b.client c " +
            "JOIN FETCH b.apartment a " +
            "WHERE b.status = " + ACTIVE + " AND b.departureDate BETWEEN :start AND :end")
    List<Booking> findDeparturesInMonth(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT b FROM Booking b JOIN FETCH b.client c JOIN FETCH b.apartment a " +
            "WHERE b.status = " + ACTIVE + " AND b.arrivalDate = :date")
    List<Booking> findArrivalsByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b JOIN FETCH b.client c JOIN FETCH b.apartment a " +
            "WHERE b.status = " + ACTIVE + " AND b.departureDate = :date")
    List<Booking> findDeparturesByDate(@Param("date") LocalDate date);

    @Query("SELECT b FROM Booking b WHERE b.status = " + ACTIVE
            + " AND b.arrivalDate <= :end AND b.departureDate > :start")
    List<Booking> findBookingsInRange(@Param("start") LocalDate start, @Param("end") LocalDate end);

    List<Booking> findAllByOrderByArrivalDateAsc();

    @EntityGraph(attributePaths = {"client", "apartment"})
    List<Booking> findAllByStatusNotOrderByArrivalDateAsc(BookingStatus status);

    @EntityGraph(attributePaths = {"client", "apartment"})
    List<Booking> findAllByStatusNot(BookingStatus status);

    /** El dashboard filtrado necesita las mismas relaciones en una sola consulta. */
    @Override
    @EntityGraph(attributePaths = {"client", "apartment"})
    Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);

    long countByStatusNot(BookingStatus status);

    long countByApartmentIdAndStatusNot(UUID apartmentId, BookingStatus status);

    Optional<Booking> findTopByClientAndStatusNotOrderByArrivalDateDesc(Client client, BookingStatus status);

    @Modifying
    @Query("UPDATE Booking b SET b.status = :archived " +
            "WHERE b.status = :active AND b.departureDate < :date")
    int archiveExpiredBookings(@Param("date") LocalDate date,
                               @Param("active") BookingStatus active,
                               @Param("archived") BookingStatus archived);
}
