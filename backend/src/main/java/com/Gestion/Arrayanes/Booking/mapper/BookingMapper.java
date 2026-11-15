package com.Gestion.Arrayanes.Booking.mapper;

import com.Gestion.Arrayanes.Apartment.model.Apartment;
import com.Gestion.Arrayanes.Booking.dto.BookingRequestDTO;
import com.Gestion.Arrayanes.Booking.dto.BookingResponseDTO;
import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Clients.model.Client;
import org.springframework.stereotype.Component;

@Component
public class BookingMapper {

    public  Booking toEntity(BookingRequestDTO bookingDto, Client client, Apartment apartment){

        Booking booking = new Booking();

        booking.setArrivalDate(bookingDto.getArrivalDate());
        booking.setNights(bookingDto.getNights());
        booking.setDepartureDate(bookingDto.getDepartureDate());
        booking.setClientId(client);
        booking.setApartmentId(apartment);
        booking.setAmountPeople(bookingDto.getAmountPeople());
        booking.setTotalPrice(bookingDto.getTotalPrice());
        return booking;
    }

    public  BookingResponseDTO toResponseDTO(Booking booking){
        BookingResponseDTO dto = new BookingResponseDTO();
        dto.setId(booking.getId());
        dto.setArrivalDate(booking.getArrivalDate());
        dto.setNights(booking.getNights());
        dto.setDepartureDate(booking.getDepartureDate());
        dto.setClientId(booking.getClientId().getId());
        dto.setApartmentId(booking.getApartmentId().getId());
        dto.setAmountPeople(booking.getAmountPeople());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setTransactionCode(booking.getTransactionCode());
        return dto;
    }

    public  void bookingUpdate(Booking booking, BookingRequestDTO bookingDto, Client client, Apartment apartment){
        booking.setArrivalDate(bookingDto.getArrivalDate());
        booking.setNights(bookingDto.getNights());
        booking.setDepartureDate(bookingDto.getDepartureDate());
        booking.setClientId(client);
        booking.setApartmentId(apartment);
        booking.setAmountPeople(bookingDto.getAmountPeople());
        booking.setTotalPrice(bookingDto.getTotalPrice());
    }
}
