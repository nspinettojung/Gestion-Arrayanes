package com.Gestion.Arrayanes.Booking.repository;

import com.Gestion.Arrayanes.Booking.model.BookingCodeSequence;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookingCodeSequenceRepository extends JpaRepository<BookingCodeSequence, Integer> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM BookingCodeSequence s WHERE s.id = :id")
    Optional<BookingCodeSequence> findByIdForUpdate(@Param("id") Integer id);
}
