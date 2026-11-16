package com.Gestion.Arrayanes.Clients.service;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.mapper.ClientMapper;
import com.Gestion.Arrayanes.Clients.model.Client;
import com.Gestion.Arrayanes.Clients.repository.ClientRepository;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock
    private ClientMapper clientMapper;

    @InjectMocks
    private ClientService clientService;

    private UUID clientId;
    private Client client;
    private ClientRequestDTO request;
    private ClientResponseDTO response;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = new Client(clientId, "Ana", "Perez", "12345678", "555-1234");
        request = new ClientRequestDTO("Ana", "Perez", "12345678", "555-1234");
        response = new ClientResponseDTO(clientId, "Ana", "Perez", "12345678", "555-1234", null);
    }

    @Test
    void saveClientShouldPersistNewClient() {
        when(clientRepository.existsByClientDni("12345678")).thenReturn(false);
        when(clientMapper.toEntity(request)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        ClientResponseDTO result = clientService.saveClient(request);

        assertEquals(response, result);
        verify(clientRepository).save(client);
    }

    @Test
    void saveClientShouldLookUpNormalizedDni() {
        ClientRequestDTO lowercase = new ClientRequestDTO("Ana", "Perez", " ab-123 ", "555-1234");
        when(clientRepository.existsByClientDni("AB-123")).thenReturn(false);
        when(clientMapper.toEntity(lowercase)).thenReturn(client);
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        clientService.saveClient(lowercase);

        verify(clientRepository).existsByClientDni("AB-123");
    }

    @Test
    void saveClientShouldRejectDuplicatedDni() {
        when(clientRepository.existsByClientDni("12345678")).thenReturn(true);

        assertThrows(GlobalCustomException.class, () -> clientService.saveClient(request));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void deleteClientShouldDeleteExistingClient() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        clientService.deleteClient(clientId);

        verify(clientRepository).delete(client);
    }

    @Test
    void deleteClientShouldFailWhenClientDoesNotExist() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.deleteClient(clientId));
    }

    @Test
    void getClientListShouldReturnEmptyListWhenThereAreNoClients() {
        when(clientRepository.findAll()).thenReturn(List.of());

        assertEquals(List.of(), clientService.getClientList());
    }

    @Test
    void getClientListShouldMapAllClients() {
        when(clientRepository.findAll()).thenReturn(List.of(client));
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        assertEquals(List.of(response), clientService.getClientList());
    }

    @Test
    void findClientByIdShouldMapClient() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        assertEquals(response, clientService.findClientById(clientId));
    }

    @Test
    void findClientByIdShouldFailWhenClientDoesNotExist() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> clientService.findClientById(clientId));
    }

    @Test
    void updateClientShouldPersistChanges() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        ClientResponseDTO result = clientService.updateClient(clientId, request);

        assertEquals(response, result);
        verify(clientMapper).updateEntity(client, request);
        verify(clientRepository, never()).existsByClientDni(any());
    }

    @Test
    void updateClientShouldIgnoreCaseAndSpacesWhenDniDoesNotChange() {
        client.setClientDni("AB-123");
        ClientRequestDTO sameDni = new ClientRequestDTO("Ana", "Perez", " ab-123 ", "555-1234");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(client)).thenReturn(client);
        when(clientMapper.toResponseDTO(client)).thenReturn(response);

        clientService.updateClient(clientId, sameDni);

        verify(clientRepository, never()).existsByClientDni(any());
        verify(clientMapper).updateEntity(client, sameDni);
    }

    @Test
    void updateClientShouldRejectDuplicatedDni() {
        ClientRequestDTO updated = new ClientRequestDTO("Ana", "Perez", "87654321", "555-1234");
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.existsByClientDni("87654321")).thenReturn(true);

        assertThrows(GlobalCustomException.class, () -> clientService.updateClient(clientId, updated));

        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void updateClientShouldFailWhenClientDoesNotExist() {
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> clientService.updateClient(clientId, request));
    }
}
