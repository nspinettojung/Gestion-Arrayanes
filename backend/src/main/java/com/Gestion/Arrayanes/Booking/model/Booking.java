package com.Gestion.Arrayanes.Booking.model;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Clients.model.Client;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
@Entity
@Table(name = "booking", indexes = {
        @Index(name = "idx_booking_arrival_date", columnList = "arrival_date"),
        @Index(name = "idx_booking_departure_date", columnList = "departure_date"),
        @Index(name = "idx_booking_apartment_departure", columnList = "apartment_id, departure_date")
})
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "UUID")
    private UUID id;
    @Column(name = "arrival_date")
    private LocalDate arrivalDate;
    @Column(name = "nights")
    private Long nights;
    @Column(name = "departure_date")
    private LocalDate departureDate;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;
    @ManyToOne
    @JoinColumn(name = "apartment_id", nullable = false)
    private Apartment apartment;
    @Column(name = "amount_people")
    private Long amountPeople;
    @Column(name = "total_price")
    private BigDecimal totalPrice;
    @Column(name = "transaction_code", unique = true, length = 32)
    private String transactionCode;
    @Version
    @Column(name = "version", nullable = false, columnDefinition = "bigint default 0")
    private long version;

    public Booking() {
    }

    public Booking(UUID id, LocalDate arrivalDate, Long nights, LocalDate departureDate, Client client, Apartment apartment, Long amountPeople, BigDecimal totalPrice) {
        this.id = id;
        this.arrivalDate = arrivalDate;
        this.nights = nights;
        this.departureDate = departureDate;
        this.client = client;
        this.apartment = apartment;
        this.amountPeople = amountPeople;
        this.totalPrice = totalPrice;
    }

}
