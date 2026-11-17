package com.Gestion.Arrayanes.Apartment.controller;

import com.Gestion.Arrayanes.Apartment.dto.ApartmentRequestDTO;
import com.Gestion.Arrayanes.Apartment.dto.ApartmentResponseDTO;
import com.Gestion.Arrayanes.Apartment.service.IApartmentService;
import com.Gestion.Arrayanes.GlobalHandlerException.GlobalCustomException;
import com.Gestion.Arrayanes.GlobalHandlerException.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ApartmentController.class)
class ApartmentControllerTest {

    private static final UUID APARTMENT_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IApartmentService apartmentService;

    @Test
    void saveApartmentShouldReturnCreatedWithLocation() throws Exception {
        when(apartmentService.saveApartment(any(ApartmentRequestDTO.class))).thenReturn(response());

        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apartmentJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith("/api/v1/apartments/" + APARTMENT_ID)))
                .andExpect(jsonPath("$.apartmentName").value("Arrayanes 1"));
    }

    @Test
    void saveApartmentShouldRejectMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void saveApartmentShouldMapDuplicateNameTo409() throws Exception {
        when(apartmentService.saveApartment(any(ApartmentRequestDTO.class)))
                .thenThrow(new GlobalCustomException("Ya existe un apartamento con ese nombre."));

        mockMvc.perform(post("/api/v1/apartments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apartmentJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void getApartmentListShouldReturnApartments() throws Exception {
        when(apartmentService.getApartmentList()).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/apartments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(APARTMENT_ID.toString()));
    }

    @Test
    void getApartmentListShouldReturnEmptyArrayWhenThereAreNoApartments() throws Exception {
        when(apartmentService.getApartmentList()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/apartments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getApartmentByIdShouldReturnNotFoundWhenMissing() throws Exception {
        when(apartmentService.findApartmentById(APARTMENT_ID))
                .thenThrow(new ResourceNotFoundException("No se encontró el apartamento"));

        mockMvc.perform(get("/api/v1/apartments/{id}", APARTMENT_ID))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateApartmentShouldReturnUpdatedApartment() throws Exception {
        when(apartmentService.updateApartment(eq(APARTMENT_ID), any(ApartmentRequestDTO.class)))
                .thenReturn(response());

        mockMvc.perform(put("/api/v1/apartments/{id}", APARTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(apartmentJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(APARTMENT_ID.toString()));
    }

    @Test
    void updatePriceShouldReturnUpdatedApartment() throws Exception {
        when(apartmentService.editPrice(APARTMENT_ID, new BigDecimal("12000"))).thenReturn(response());

        mockMvc.perform(patch("/api/v1/apartments/{id}/price", APARTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"price\":12000}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(APARTMENT_ID.toString()));
    }

    @Test
    void updateNameShouldReturnUpdatedApartment() throws Exception {
        when(apartmentService.editApartName(APARTMENT_ID, "Nuevo")).thenReturn(response());

        mockMvc.perform(patch("/api/v1/apartments/{id}/name", APARTMENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"apartmentName\":\"Nuevo\"}"))
                .andExpect(status().isOk());
    }

    @Test
    void readEndpointsShouldReturnValues() throws Exception {
        when(apartmentService.getPriceById(APARTMENT_ID)).thenReturn(new BigDecimal("10000.00"));
        when(apartmentService.getApartmentName(APARTMENT_ID)).thenReturn("Arrayanes 1");
        when(apartmentService.getApartmentMaxNumber(APARTMENT_ID)).thenReturn(4L);

        mockMvc.perform(get("/api/v1/apartments/{id}/price", APARTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("10000.00"));

        mockMvc.perform(get("/api/v1/apartments/{id}/name", APARTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("Arrayanes 1"));

        mockMvc.perform(get("/api/v1/apartments/{id}/maxnumber", APARTMENT_ID))
                .andExpect(status().isOk())
                .andExpect(content().string("4"));
    }

    @Test
    void deleteApartmentShouldReturnNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/apartments/{id}", APARTMENT_ID))
                .andExpect(status().isNoContent());

        verify(apartmentService).deleteApartment(APARTMENT_ID);
    }

    @Test
    void deleteApartmentShouldReturnConflictWhenItHasBookings() throws Exception {
        doThrow(new GlobalCustomException(
                        "No se puede eliminar el departamento porque tiene reservas asociadas."))
                .when(apartmentService).deleteApartment(APARTMENT_ID);

        mockMvc.perform(delete("/api/v1/apartments/{id}", APARTMENT_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail")
                        .value("No se puede eliminar el departamento porque tiene reservas asociadas."))
                .andExpect(jsonPath("$.status").value(409));
    }

    private ApartmentResponseDTO response() {
        return new ApartmentResponseDTO(APARTMENT_ID, "Arrayanes 1", 4L, new BigDecimal("10000.00"));
    }

    private String apartmentJson() {
        return """
                {
                  "apartmentName": "Arrayanes 1",
                  "apartmentMaxNumber": 4,
                  "price": 10000.00
                }
                """;
    }
}
