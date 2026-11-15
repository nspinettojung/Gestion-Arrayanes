package com.Gestion.Arrayanes.Clients.controller;

import com.Gestion.Arrayanes.Clients.dto.ClientRequestDTO;
import com.Gestion.Arrayanes.Clients.dto.ClientResponseDTO;
import com.Gestion.Arrayanes.Clients.service.IClientService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    private final IClientService iClientService;

    public ClientController(IClientService iClientService) {
        this.iClientService = iClientService;
    }

    @PostMapping("/createclient")
    public ResponseEntity<ClientResponseDTO> createClient(@Valid @RequestBody ClientRequestDTO requestClient) {
        ClientResponseDTO client = iClientService.saveClient(requestClient);
        URI location = URI.create("/api/clients/createclient/" + client.getId());
        return ResponseEntity.created(location).body(client);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteClient(@PathVariable UUID id) {
        iClientService.deleteClient(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/listclient")
    public ResponseEntity<List<ClientResponseDTO>> getClientList() {
        return ResponseEntity.ok(iClientService.getClientList());
    }

    @GetMapping("/client/{id}")
    public ResponseEntity<ClientResponseDTO> getClientById(@PathVariable UUID id) {
        return ResponseEntity.ok(iClientService.findClientById(id));
    }

    @PutMapping("/editclient/{id}")
    public ResponseEntity<ClientResponseDTO> updateClient(@PathVariable UUID id,
                                                          @Valid @RequestBody ClientRequestDTO clientRequest) {
        return ResponseEntity.ok(iClientService.updateClient(id, clientRequest));
    }


}
