package com.Gestion.Arrayanes.Clients.service;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.mapper.ClientMapper;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.repository.ClientRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ClientService implements IClientService {

    private final ClientRepository clientRepository;
    private final ClientMapper clientMapper;

    public ClientService(ClientRepository clientRepository, ClientMapper clientMapper) {
        this.clientRepository = clientRepository;
        this.clientMapper = clientMapper;
    }

    private Client getClientById(UUID id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el cliente con id: " + id));
    }

    @Override
    @Transactional
    public ClientResponseDTO saveClient(ClientRequestDTO clientRequestDTO) {
        if (clientRepository.existsByClientDni(clientRequestDTO.getClientDni())) {
            throw new GlobalCustomException("El cliente ya existe.");
        }

        Client client = clientMapper.toEntity(clientRequestDTO);
        Client saved = clientRepository.save(client);

        return clientMapper.toResponseDTO(saved);
    }

    @Override
    @Transactional
    public void deleteClient(UUID id) {
        Client client = getClientById(id);
        clientRepository.delete(client);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClientResponseDTO> getClientList() {
        return clientRepository.findAll().stream()
                .map(clientMapper::toResponseDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClientResponseDTO findClientById(UUID id) {
        return clientMapper.toResponseDTO(getClientById(id));
    }

    @Override
    @Transactional
    public ClientResponseDTO updateClient(UUID id, ClientRequestDTO clientRequestDTO) {
        Client client = getClientById(id);

        boolean dniChanged = !client.getClientDni().equals(clientRequestDTO.getClientDni());

        if (dniChanged && clientRepository.existsByClientDni(clientRequestDTO.getClientDni())) {
            throw new GlobalCustomException("Ya existe un cliente con ese DNI.");
        }

        clientMapper.updateEntity(client, clientRequestDTO);
        Client updateClient = clientRepository.save(client);

        return clientMapper.toResponseDTO(updateClient);
    }
}
