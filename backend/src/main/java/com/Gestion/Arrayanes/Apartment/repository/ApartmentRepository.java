package com.Gestion.Arrayanes.Apartment.repository;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApartmentRepository extends JpaRepository<Apartment, UUID> {

    boolean existsByApartmentName(String name);

    Optional<Apartment> findByApartmentNameIgnoreCase(String name);

}
