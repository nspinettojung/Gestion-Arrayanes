package com.Gestion.Arrayanes.DashboardDTO.model;

import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Setter
@Getter
public class DashboardDTO {

    private UUID id;
    private String transactionCode;
    private UUID clientId;
    private String clientName;
    private String clientLastName;
    private UUID apartmentId;
    private String apartmentName;
    private LocalDate arrivalDate;
    private LocalDate departureDate;
    private Long nights;
    private Long amountPeople;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private long version;

    public DashboardDTO(UUID id, String transactionCode, UUID clientId, String clientName, String clientLastName,
                        UUID apartmentId, String apartmentName, LocalDate arrivalDate, LocalDate departureDate,
                        Long nights, Long amountPeople, BigDecimal totalPrice, long version) {
        this(id, transactionCode, clientId, clientName, clientLastName, apartmentId, apartmentName,
                arrivalDate, departureDate, nights, amountPeople, totalPrice, BookingStatus.ACTIVE, version);
    }

    public DashboardDTO(UUID id, String transactionCode, UUID clientId, String clientName, String clientLastName,
                        UUID apartmentId, String apartmentName, LocalDate arrivalDate, LocalDate departureDate,
                        Long nights, Long amountPeople, BigDecimal totalPrice, BookingStatus status, long version) {
        this.id = id;
        this.transactionCode = transactionCode;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientLastName = clientLastName;
        this.apartmentId = apartmentId;
        this.apartmentName = apartmentName;
        this.arrivalDate = arrivalDate;
        this.departureDate = departureDate;
        this.nights = nights;
        this.amountPeople = amountPeople;
        this.totalPrice = totalPrice;
        this.status = status;
        this.version = version;
    }

}
