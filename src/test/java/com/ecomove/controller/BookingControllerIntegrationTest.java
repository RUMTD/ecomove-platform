package com.ecomove.controller;

import com.ecomove.model.Trip;
import com.ecomove.repository.TripRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests d integration — API REST avec base H2 en memoire
 * Verifie le pipeline complet : HTTP -> Controller -> Service -> BDD
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("BookingController — Tests d integration")
class BookingControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private TripRepository tripRepository;
    @Autowired private ObjectMapper objectMapper;

    private Trip tripDisponible;

    @BeforeEach
    void setUp() {
        tripRepository.deleteAll();

        tripDisponible = tripRepository.save(Trip.builder()
                .driverId(99L)
                .departureAddress("Paris 15e")
                .arrivalAddress("La Defense")
                .departureTime(LocalDateTime.now().plusHours(1))
                .totalSeats(4)
                .availableSeats(3)
                .co2PerKmKg(0.12)
                .status(Trip.TripStatus.ACTIVE)
                .build());
    }

    @Test
    @DisplayName("POST /bookings — 201 Created pour une reservation valide")
    void postBooking_valid_returns201() throws Exception {
        Map<String, Long> body = Map.of("userId", 1L, "tripId", tripDisponible.getId());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.co2SavedKg").isNumber());
    }

    @Test
    @DisplayName("POST /bookings — 409 Conflict si trajet complet")
    void postBooking_noSeats_returns409() throws Exception {
        // Rendre le trajet complet
        tripDisponible.setAvailableSeats(0);
        tripDisponible.setStatus(Trip.TripStatus.FULL);
        tripRepository.save(tripDisponible);

        Map<String, Long> body = Map.of("userId", 1L, "tripId", tripDisponible.getId());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("NO_SEATS"));
    }

    @Test
    @DisplayName("POST /bookings — 403 Forbidden si conducteur = passager")
    void postBooking_driverIsPassenger_returns403() throws Exception {
        // userId=99 est le conducteur du trajet
        Map<String, Long> body = Map.of("userId", 99L, "tripId", tripDisponible.getId());

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /bookings — 404 Not Found si trajet inexistant")
    void postBooking_unknownTrip_returns404() throws Exception {
        Map<String, Long> body = Map.of("userId", 1L, "tripId", 99999L);

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("POST /bookings — 400 Bad Request si userId manquant")
    void postBooking_missingUserId_returns400() throws Exception {
        String body = "{\"tripId\": 1}"; // userId absent

        mockMvc.perform(post("/api/v1/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }
}
