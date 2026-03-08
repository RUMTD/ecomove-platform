package com.ecomove.service;

import com.ecomove.exception.ForbiddenBookingException;
import com.ecomove.exception.NoSeatsAvailableException;
import com.ecomove.exception.TripNotFoundException;
import com.ecomove.model.Booking;
import com.ecomove.model.BookingRequest;
import com.ecomove.model.BookingResponse;
import com.ecomove.model.Trip;
import com.ecomove.repository.BookingRepository;
import com.ecomove.repository.TripRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service métier — Gestion des réservations de covoiturage.
 *
 * <p>Contient toute la logique métier liée à la création et à la gestion
 * des réservations sur la plateforme EcoMove. Ce service est la couche
 * centrale de l'architecture en couches : il est appelé par les controllers
 * et appelle les repositories pour l'accès aux données.</p>
 *
 * <p><strong>Règles métier appliquées :</strong></p>
 * <ol>
 *   <li>Le trajet référencé doit exister en base de données</li>
 *   <li>Un conducteur ne peut pas réserver son propre trajet</li>
 *   <li>Le trajet doit avoir au moins une place disponible</li>
 * </ol>
 *
 * @author DJAKOU TCHUETKA Ruben Magdiel
 * @version 1.0.0
 * @see BookingRepository
 * @see TripRepository
 */
@Service
@RequiredArgsConstructor
public class BookingService {

    /** Repository JPA pour la persistance des réservations. */
    private final BookingRepository bookingRepository;

    /** Repository JPA pour l'accès aux trajets. */
    private final TripRepository tripRepository;

    /**
     * Distance moyenne d'un trajet domicile-travail utilisée pour
     * le calcul des économies CO2 (en kilomètres).
     * Valeur MVP — sera remplacée par le calcul géographique réel.
     */
    private static final double DISTANCE_MOYENNE_KM = 30.0;

    /**
     * Crée une nouvelle réservation de covoiturage.
     *
     * <p>Cette méthode est transactionnelle : si une erreur survient après
     * la décrémentation des places, toutes les modifications en base sont
     * annulées automatiquement (rollback).</p>
     *
     * <p><strong>Algorithme :</strong></p>
     * <ol>
     *   <li>Récupérer le trajet depuis la BDD (404 si absent)</li>
     *   <li>Vérifier que userId != driverId (403 sinon)</li>
     *   <li>Vérifier que availableSeats > 0 (409 sinon)</li>
     *   <li>Calculer les économies CO2 = distance × co2PerKmKg</li>
     *   <li>Persister la réservation avec statut CONFIRMED</li>
     *   <li>Décrémenter availableSeats du trajet</li>
     *   <li>Passer le trajet en FULL si plus aucune place</li>
     * </ol>
     *
     * @param request DTO contenant {@code userId} et {@code tripId}, tous deux obligatoires
     * @return {@link BookingResponse} avec l'id, le statut CONFIRMED et les économies CO2
     * @throws TripNotFoundException     si {@code tripId} ne correspond à aucun trajet
     * @throws ForbiddenBookingException si {@code userId} est le conducteur du trajet
     * @throws NoSeatsAvailableException si {@code availableSeats} est égal à zéro
     */
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {

        // Étape 1 — Vérifier que le trajet existe
        Trip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new TripNotFoundException(request.getTripId()));

        // Étape 2 — Règle métier : conducteur != passager (403)
        if (trip.getDriverId().equals(request.getUserId())) {
            throw new ForbiddenBookingException();
        }

        // Étape 3 — Règle métier : places disponibles (409)
        if (trip.getAvailableSeats() <= 0) {
            throw new NoSeatsAvailableException(request.getTripId());
        }

        // Étape 4 — Calcul des économies CO2
        double co2Saved = calculerEconomiesCO2(trip.getCo2PerKmKg());

        // Étape 5 — Création de la réservation
        Booking booking = Booking.builder()
                .userId(request.getUserId())
                .tripId(request.getTripId())
                .status(Booking.BookingStatus.CONFIRMED)
                .bookingDate(LocalDateTime.now())
                .co2SavedKg(co2Saved)
                .build();

        // Étape 6 — Décrémenter les places
        trip.setAvailableSeats(trip.getAvailableSeats() - 1);

        // Étape 7 — Marquer le trajet FULL si nécessaire
        if (trip.getAvailableSeats() == 0) {
            trip.setStatus(Trip.TripStatus.FULL);
        }

        tripRepository.save(trip);
        Booking saved = bookingRepository.save(booking);

        return mapToResponse(saved);
    }

    /**
     * Calcule les économies de CO2 générées par une réservation de covoiturage.
     *
     * <p>Formule : {@code economie = distanceMoyenne × co2ParKm}</p>
     *
     * <p>Exemple : pour un trajet à 0.12 kg CO2/km sur 30 km,
     * l'économie est de 30 × 0.12 = 3.6 kg de CO2.</p>
     *
     * @param co2PerKmKg émissions CO2 par kilomètre du trajet, en kg/km
     * @return les économies CO2 en kilogrammes, toujours positives
     */
    private double calculerEconomiesCO2(double co2PerKmKg) {
        return DISTANCE_MOYENNE_KM * co2PerKmKg;
    }

    /**
     * Convertit une entité {@link Booking} en DTO {@link BookingResponse}.
     *
     * @param booking l'entité JPA persistée
     * @return le DTO prêt à être sérialisé en JSON
     */
    private BookingResponse mapToResponse(Booking booking) {
        return BookingResponse.builder()
                .id(booking.getId())
                .status(booking.getStatus().name())
                .bookingDate(booking.getBookingDate())
                .co2SavedKg(booking.getCo2SavedKg())
                .userId(booking.getUserId())
                .tripId(booking.getTripId())
                .build();
    }
}
