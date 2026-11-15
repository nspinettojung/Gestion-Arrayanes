package com.Gestion.Arrayanes.Clients.service;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;

import java.util.List;
import java.util.UUID;

public interface IClientService {

    ClientResponseDTO saveClient(ClientRequestDTO clientRequestDTO);

    void deleteClient(UUID id);

    List<ClientResponseDTO> getClientList();

    ClientResponseDTO findClientById(UUID id);

    ClientResponseDTO updateClient(UUID id, ClientRequestDTO clientRequestDTO);
}
