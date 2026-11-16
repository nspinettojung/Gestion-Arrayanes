package com.Gestion.Arrayanes.Clients.mapper;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.util.ClientNormalization;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class ClientMapper {

    private final BookingRepository bookingRepository;

    public ClientMapper(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public  Client toEntity(ClientRequestDTO clientDto){

        Client client = new Client();

        client.setClientName(clientDto.getClientName());
        client.setClientLastName(clientDto.getClientLastName());
        client.setClientDni(ClientNormalization.normalizeDni(clientDto.getClientDni()));
        client.setClientPhone(clientDto.getClientPhone());

        return client;
    }

    public ClientResponseDTO toResponseDTO(Client client){

        ClientResponseDTO clientDto = new ClientResponseDTO();

        clientDto.setId(client.getId());
        clientDto.setClientName(client.getClientName());
        clientDto.setClientLastName(client.getClientLastName());
        clientDto.setClientDni(client.getClientDni());
        clientDto.setClientPhone(client.getClientPhone());

        Optional<Booking> lastBooking = bookingRepository.findTopByClientIdOrderByArrivalDateDesc(client);

        lastBooking.ifPresent(b -> clientDto.setLastCheckIn(b.getArrivalDate()));

        return clientDto;
    }



    public  void updateEntity(Client client,
                            ClientRequestDTO clientDto){

        client.setClientName(clientDto.getClientName());
        client.setClientLastName(clientDto.getClientLastName());
        client.setClientDni(ClientNormalization.normalizeDni(clientDto.getClientDni()));
        client.setClientPhone(clientDto.getClientPhone());
    }
}
