package com.Gestion.Arrayanes.Clients.controller;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.service.IClientService;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClientController.class)
class ClientControllerTest {

    private static final UUID CLIENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IClientService clientService;

    @Test
    void createClientShouldReturnCreatedWithLocation() throws Exception {
        when(clientService.saveClient(any(ClientRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ana")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/clients/createclient/" + CLIENT_ID)))
                .andExpect(jsonPath("$.clientName").value("Ana"));
    }

    @Test
    void createClientShouldRejectBlankName() throws Exception {
        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson(" ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createClientShouldRejectMalformedJson() throws Exception {
        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{not-json}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createClientShouldRejectMalformedDni() throws Exception {
        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ana", "12", "555-1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("DNI o pasaporte")));
    }

    @Test
    void createClientShouldRejectMalformedPhone() throws Exception {
        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ana", "12345678", "abc")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value(containsString("teléfono")));
    }

    @Test
    void createClientShouldAcceptDottedDniAndInternationalPhone() throws Exception {
        when(clientService.saveClient(any(ClientRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/clients/createclient")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ana", "12.345.678", "+54 9 11 5555-1234")))
                .andExpect(status().isCreated());
    }

    @Test
    void getClientListShouldReturnClients() throws Exception {
        when(clientService.getClientList()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/clients/listclient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(CLIENT_ID.toString()))
                .andExpect(jsonPath("$[0].lastCheckIn").value("2026-10-05"));
    }

    @Test
    void getClientListShouldReturnEmptyArrayWhenThereAreNoClients() throws Exception {
        when(clientService.getClientList()).thenReturn(List.of());

        mockMvc.perform(get("/api/clients/listclient"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getClientByIdShouldReturnClient() throws Exception {
        when(clientService.findClientById(CLIENT_ID)).thenReturn(response());

        mockMvc.perform(get("/api/clients/client/{id}", CLIENT_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientDni").value("12345678"));
    }

    @Test
    void getClientByIdShouldReturnNotFound() throws Exception {
        when(clientService.findClientById(CLIENT_ID))
                .thenThrow(new ResourceNotFoundException("No se encontró el cliente"));

        mockMvc.perform(get("/api/clients/client/{id}", CLIENT_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void updateClientShouldReturnUpdatedClient() throws Exception {
        when(clientService.updateClient(eq(CLIENT_ID), any(ClientRequestDTO.class))).thenReturn(response());

        mockMvc.perform(put("/api/clients/editclient/{id}", CLIENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(clientJson("Ana")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(CLIENT_ID.toString()));
    }

    @Test
    void deleteClientShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/clients/delete/{id}", CLIENT_ID))
                .andExpect(status().isNoContent());

        verify(clientService).deleteClient(CLIENT_ID);
    }

    private ClientResponseDTO response() {
        return new ClientResponseDTO(
                CLIENT_ID, "Ana", "Perez", "12345678", "555-1234", LocalDate.of(2026, 10, 5));
    }

    private String clientJson(String name) {
        return clientJson(name, "12345678", "555-1234");
    }

    private String clientJson(String name, String dni, String phone) {
        return """
                {
                  "clientName": "%s",
                  "clientLastName": "Perez",
                  "clientDni": "%s",
                  "clientPhone": "%s"
                }
                """.formatted(name, dni, phone);
    }
}
