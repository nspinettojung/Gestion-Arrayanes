package com.Gestion.Arrayanes.Clients.mapper;

import com.Gestion.Arrayanes.Booking.model.Booking;
import com.Gestion.Arrayanes.Booking.model.BookingStatus;
import com.Gestion.Arrayanes.Booking.repository.BookingRepository;
import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.util.ClientNormalization;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

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

    /** Para operaciones de un solo cliente; el listado usa la variante con mapa. */
    public ClientResponseDTO toResponseDTO(Client client) {
        Optional<Booking> lastBooking = bookingRepository
                .findTopByClientAndStatusNotOrderByArrivalDateDesc(client, BookingStatus.CANCELLED);

        return toResponseDTO(client,
                lastBooking.map(Booking::getArrivalDate).orElse(null));
    }

    /** El mapa viene de una consulta agrupada: una sola query para todo el listado. */
    public ClientResponseDTO toResponseDTO(Client client, Map<UUID, LocalDate> lastCheckIns) {
        return toResponseDTO(client,
                lastCheckIns == null ? null : lastCheckIns.get(client.getId()));
    }

    private ClientResponseDTO toResponseDTO(Client client, LocalDate lastCheckIn) {
        ClientResponseDTO clientDto = new ClientResponseDTO();

        clientDto.setId(client.getId());
        clientDto.setClientName(client.getClientName());
        clientDto.setClientLastName(client.getClientLastName());
        clientDto.setClientDni(client.getClientDni());
        clientDto.setClientPhone(client.getClientPhone());
        clientDto.setLastCheckIn(lastCheckIn);

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
