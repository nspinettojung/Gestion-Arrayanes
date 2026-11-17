package com.Gestion.Arrayanes.Booking.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "booking_code_sequence")
@Getter
@Setter
@NoArgsConstructor
public class BookingCodeSequence {

    @Id
    private Integer id;

    @Column(name = "sequence_year", nullable = false)
    private Integer year;

    @Column(name = "next_value", nullable = false)
    private Long nextValue;

    public BookingCodeSequence(Integer id, Integer year, Long nextValue) {
        this.id = id;
        this.year = year;
        this.nextValue = nextValue;
    }
}
