package com.Gestion.Arrayanes.Booking.mapper;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingBaseRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    /**
     * Copia solo los datos que envía el cliente. El precio total lo calcula el
     * servicio a partir del precio del apartamento y la estadía.
     */
    public Booking toEntity(BookingBaseRequestDTO bookingDto, Client client, Apartment apartment) {

        Booking booking = new Booking();

        booking.setArrivalDate(bookingDto.getArrivalDate());
        booking.setNights(bookingDto.getNights());
        booking.setDepartureDate(bookingDto.getDepartureDate());
        booking.setClient(client);
        booking.setApartment(apartment);
        booking.setAmountPeople(bookingDto.getAmountPeople());
        return booking;
    }

    public BookingResponseDTO toResponseDTO(Booking booking) {
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setArrivalDate(booking.getArrivalDate());
        dto.setNights(booking.getNights());
        dto.setDepartureDate(booking.getDepartureDate());
        dto.setClientId(booking.getClient().getId());
        dto.setClientName(booking.getClient().getClientName());
        dto.setApartmentId(booking.getApartment().getId());
        dto.setApartmentName(booking.getApartment().getApartmentName());
        dto.setAmountPeople(booking.getAmountPeople());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setTransactionCode(booking.getTransactionCode());
        dto.setVersion(booking.getVersion());
        return dto;
    }

    /**
     * Aplica los cambios de la reserva. El precio total lo recalcula el
     * servicio con los datos vigentes del apartamento.
     */
    public void bookingUpdate(Booking booking, BookingBaseRequestDTO bookingDto, Client client, Apartment apartment) {
        booking.setArrivalDate(bookingDto.getArrivalDate());
        booking.setNights(bookingDto.getNights());
        booking.setDepartureDate(bookingDto.getDepartureDate());
        booking.setClient(client);
        booking.setApartment(apartment);
        booking.setAmountPeople(bookingDto.getAmountPeople());
    }
}
