package com.Gestion.Arrayanes.Apartment.controller;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;
import com.Gestion.Arrayanes.Apartment.service.IApartmentService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/apartments")
public class ApartmentController {

    private final IApartmentService iApartmentService;

    public ApartmentController(IApartmentService iApartmentService) {
        this.iApartmentService = iApartmentService;
    }

    @PostMapping
    public ResponseEntity<ApartmentResponseDTO> saveApartment(@Valid @RequestBody ApartmentRequestDTO requestApartment) {
        ApartmentResponseDTO apartment = iApartmentService.saveApartment(requestApartment);
        URI location = URI.create("/api/v1/apartments/" + apartment.getId());
        return ResponseEntity.created(location).body(apartment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApartment(@PathVariable UUID id) {
        iApartmentService.deleteApartment(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ApartmentResponseDTO>> getApartmentList() {
        return ResponseEntity.ok(iApartmentService.getApartmentList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApartmentResponseDTO> getApartmentById(@PathVariable UUID id) {
        return ResponseEntity.ok(iApartmentService.findApartmentById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApartmentResponseDTO> updateApartment(@PathVariable UUID id,
                                                                @Valid @RequestBody ApartmentRequestDTO apartmentRequestDTO) {
        return ResponseEntity.ok(iApartmentService.updateApartment(id, apartmentRequestDTO));
    }

    @GetMapping("/{id}/maxnumber")
    public ResponseEntity<Long> getApartmentMaxNumber(@PathVariable UUID id) {
        return ResponseEntity.ok(iApartmentService.getApartmentMaxNumber(id));
    }

    @PatchMapping("/{id}/price")
    public ResponseEntity<ApartmentResponseDTO> updatePrice(@PathVariable UUID id, @RequestBody Map<String, BigDecimal> body) {
        BigDecimal newPrice = body.get("price");
        return ResponseEntity.ok(iApartmentService.editPrice(id, newPrice));
    }

    @GetMapping("/{id}/price")
    public ResponseEntity<BigDecimal> getPriceById(@PathVariable UUID id) {
        return ResponseEntity.ok(iApartmentService.getPriceById(id));
    }

    @GetMapping("/{id}/name")
    public ResponseEntity<String> getApartmentNameById(@PathVariable UUID id) {
        return ResponseEntity.ok(iApartmentService.getApartmentName(id));
    }

    @PatchMapping("/{id}/name")
    public ResponseEntity<ApartmentResponseDTO> updateApartmentName(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        String newName = body.get("apartmentName");
        return ResponseEntity.ok(iApartmentService.editApartName(id, newName));
    }

}
