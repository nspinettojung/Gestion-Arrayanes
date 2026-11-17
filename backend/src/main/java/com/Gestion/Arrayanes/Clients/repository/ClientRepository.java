package com.Gestion.Arrayanes.Clients.repository;

import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClientRepository extends JpaRepository<Client, UUID> {

    boolean existsByClientDni(String clientDni);

    Optional<Client> findByClientDni(String clientDni);

    /**
     * Una sola consulta para todos los clientes: evita repetir el "top 1" por
     * cliente al armar el listado. Las reservas canceladas no cuentan.
     */
    @Query("SELECT c.id AS clientId, MAX(b.arrivalDate) AS lastCheckIn " +
            "FROM Client c LEFT JOIN Booking b ON b.client = c AND b.status <> :status " +
            "GROUP BY c.id")
    List<ClientLastCheckIn> findLastCheckIns(@Param("status") BookingStatus status);
}
