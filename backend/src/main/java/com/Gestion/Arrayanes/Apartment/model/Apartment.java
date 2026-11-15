package com.Gestion.Arrayanes.Apartment.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "apartment", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"apartment_name", "apartment_max_number"})
})
public class Apartment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(name = "apartment_name")
    private String apartmentName;
    @Column(name = "apartment_max_number")
    private Long apartmentMaxNumber;
    @Column(name = "price")
    private BigDecimal price;


    public Apartment() {
    }

    public Apartment(UUID id, String apartmentName, Long apartmentMaxNumber, BigDecimal price) {
        this.id = id;
        this.apartmentName = apartmentName;
        this.apartmentMaxNumber = apartmentMaxNumber;
        this.price = price;
    }

}
