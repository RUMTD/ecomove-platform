package com.ecomove.service;

import com.ecomove.exception.ForbiddenBookingException;
import com.ecomove.exception.NoSeatsAvailableException;
import com.ecomove.exception.TripNotFoundException;
import com.ecomove.model.*;
import com.ecomove.repository.BookingRepository;
import com.ecomove.repository.TripRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires de BookingService — Approche TDD
 * Couverture : cas nominal, erreur metier, cas limite
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BookingService — Tests unitaires TDD")
class BookingServiceTest {

    @Mock private TripRepository    tripRepository;
    @Mock private BookingRepository bookingRepository;
    @InjectMocks private BookingService bookingService;

    private Trip tripAvecPlaces;
    private Trip tripSansPlaces;
    private Trip tripDuConducteur;

    @BeforeEach
    void setUp() {
        // Trajet avec 3 places disponibles
        tripAvecPlaces = Trip.builder()
                .id(10L)
                .driverId(99L)          // conducteur = userId 99
                .availableSeats(3)
                .totalSeats(4)
                .co2PerKmKg(0.12)
                .status(Trip.TripStatus.ACTIVE)
                .departureTime(LocalDateTime.now().plusHours(2))
                .build();

        // Trajet complet (0 places)
        tripSansPlaces = Trip.builder()
                .id(20L)
                .driverId(99L)
                .availableSeats(0)
                .totalSeats(4)
                .co2PerKmKg(0.12)
                .status(Trip.TripStatus.FULL)
                .build();

        // Trajet dont le conducteur est userId=1
        tripDuConducteur = Trip.builder()
                .id(5L)
                .driverId(1L)           // conducteur = userId 1
                .availableSeats(3)
                .totalSeats(4)
                .co2PerKmKg(0.12)
                .status(Trip.TripStatus.ACTIVE)
                .build();
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CAS NOMINAL
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("CAS NOMINAL : doit creer une reservation et retourner CONFIRMED")
    void createBooking_casNominal_doitRetournerConfirmed() {
        // GIVEN — employe authentifie (userId=1), trajet avec 3 places (tripId=10)
        BookingRequest request = new BookingRequest(1L, 10L);

        Booking bookingSaved = Booking.builder()
                .id(789L)
                .userId(1L)
                .tripId(10L)
                .status(Booking.BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now())
                .co2SavedKg(3.6)
                .build();

        when(tripRepository.findById(10L)).thenReturn(Optional.of(tripAvecPlaces));
        when(bookingRepository.save(any(Booking.class))).thenReturn(bookingSaved);
        when(tripRepository.save(any(Trip.class))).thenReturn(tripAvecPlaces);

        // WHEN
        BookingResponse response = bookingService.createBooking(request);

        // THEN
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getId()).isEqualTo(789L);
        assertThat(response.getCo2SavedKg()).isGreaterThan(0);

        // Verification : les places ont bien ete decrementees
        verify(tripRepository).save(argThat(t -> t.getAvailableSeats() == 2));
        verify(bookingRepository).save(any(Booking.class));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CAS ERREUR METIER — 403 FORBIDDEN
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("CAS ERREUR METIER : conducteur ne peut pas reserver son propre trajet")
    void createBooking_conducteurReservesonTrajet_doitLancerForbidden() {
        // GIVEN — userId=1 est le conducteur du trajet tripId=5
        BookingRequest request = new BookingRequest(1L, 5L);
        when(tripRepository.findById(5L)).thenReturn(Optional.of(tripDuConducteur));

        // WHEN + THEN
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(ForbiddenBookingException.class)
                .hasMessageContaining("conducteur");

        // Verification : aucune reservation creee
        verify(bookingRepository, never()).save(any());
        verify(tripRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CAS LIMITE — 409 CONFLICT (plus de places)
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("CAS LIMITE : trajet complet doit retourner NoSeatsAvailableException")
    void createBooking_trajetComplet_doitLancerNoSeats() {
        // GIVEN — trajet avec 0 places restantes (tripId=20)
        BookingRequest request = new BookingRequest(2L, 20L);
        when(tripRepository.findById(20L)).thenReturn(Optional.of(tripSansPlaces));

        // WHEN + THEN
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(NoSeatsAvailableException.class)
                .hasMessageContaining("Plus de places");

        // Verification : aucune reservation creee, aucune modification du trajet
        verify(bookingRepository, never()).save(any());
        verify(tripRepository, never()).save(any());
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CAS ERREUR — 404 NOT FOUND
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("CAS ERREUR : trajet inexistant doit retourner TripNotFoundException")
    void createBooking_trajetInexistant_doitLancerNotFound() {
        // GIVEN — tripId=999 n'existe pas en base
        BookingRequest request = new BookingRequest(1L, 999L);
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        // WHEN + THEN
        assertThatThrownBy(() -> bookingService.createBooking(request))
                .isInstanceOf(TripNotFoundException.class)
                .hasMessageContaining("999");
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CAS LIMITE — Derniere place : trajet doit passer en FULL
    // ═══════════════════════════════════════════════════════════════════════
    @Test
    @DisplayName("CAS LIMITE : si c est la derniere place, le trajet passe en FULL")
    void createBooking_dernierePlace_trajetPasseEnFull() {
        // GIVEN — trajet avec exactement 1 place restante
        Trip trajetAvec1Place = Trip.builder()
                .id(30L).driverId(99L).availableSeats(1)
                .totalSeats(4).co2PerKmKg(0.12)
                .status(Trip.TripStatus.ACTIVE).build();

        BookingRequest request = new BookingRequest(1L, 30L);
        Booking saved = Booking.builder().id(100L).status(Booking.BookingStatus.CONFIRMED)
                .userId(1L).tripId(30L).bookingDate(LocalDateTime.now()).co2SavedKg(3.6).build();

        when(tripRepository.findById(30L)).thenReturn(Optional.of(trajetAvec1Place));
        when(bookingRepository.save(any())).thenReturn(saved);
        when(tripRepository.save(any())).thenReturn(trajetAvec1Place);

        // WHEN
        bookingService.createBooking(request);

        // THEN — le trajet doit etre sauvegarde avec status FULL
        verify(tripRepository).save(argThat(t ->
                t.getAvailableSeats() == 0 && t.getStatus() == Trip.TripStatus.FULL));
    }
}
