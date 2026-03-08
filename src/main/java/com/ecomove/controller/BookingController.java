package com.ecomove.controller;

import com.ecomove.exception.ForbiddenBookingException;
import com.ecomove.exception.NoSeatsAvailableException;
import com.ecomove.exception.TripNotFoundException;
import com.ecomove.model.BookingRequest;
import com.ecomove.model.BookingResponse;
import com.ecomove.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST — Gestion des réservations EcoMove.
 *
 * <p>Expose les endpoints HTTP pour créer et gérer les réservations
 * de covoiturage entre employés d'entreprises partenaires.</p>
 *
 * <p>Toutes les requêtes nécessitent un token JWT valide dans le header
 * {@code Authorization: Bearer <token>}.</p>
 *
 * <p>Base URL : {@code /api/v1/bookings}</p>
 *
 * @author DJAKOU TCHUETKA Ruben Magdiel
 * @version 1.0.0
 * @see BookingService
 */
@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(
    name = "Réservations",
    description = "Création et gestion des réservations de covoiturage"
)
public class BookingController {

    private final BookingService bookingService;

    /**
     * Crée une nouvelle réservation de covoiturage.
     *
     * <p>Règles métier appliquées :</p>
     * <ul>
     *   <li>Le trajet doit exister en base de données</li>
     *   <li>Le conducteur ne peut pas réserver son propre trajet</li>
     *   <li>Il doit rester au moins une place disponible</li>
     * </ul>
     *
     * <p>En cas de succès, les {@code availableSeats} du trajet sont
     * automatiquement décrémentés, et les économies CO2 sont calculées.</p>
     *
     * @param request le corps de la requête contenant {@code userId} et {@code tripId}
     * @return 201 avec la réservation créée, ou 4xx/5xx selon l'erreur
     * @throws TripNotFoundException      si le {@code tripId} n'existe pas (404)
     * @throws ForbiddenBookingException  si le conducteur réserve son propre trajet (403)
     * @throws NoSeatsAvailableException  si le trajet est complet (409)
     */
    @Operation(
        summary = "Créer une réservation",
        description = """
            Crée une réservation pour un employé authentifié sur un trajet disponible.
            
            **Pré-conditions :**
            - Token JWT valide dans le header Authorization
            - Le trajet doit exister et avoir des places disponibles
            - L'utilisateur ne doit pas être le conducteur du trajet
            
            **Effets :**
            - Réservation enregistrée en base de données avec statut CONFIRMED
            - Places disponibles du trajet décrémentées
            - Économies CO2 calculées et stockées
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Réservation créée avec succès",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = BookingResponse.class),
                examples = @ExampleObject(value = """
                    {
                      "id": 789,
                      "status": "CONFIRMED",
                      "bookingDate": "2026-03-09T08:30:00",
                      "co2SavedKg": 3.6,
                      "userId": 1,
                      "tripId": 10
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Paramètres invalides (userId ou tripId manquant/négatif)",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "INVALID_PARAMS", "message": "userId est obligatoire"}
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "401",
            description = "Token JWT absent ou expiré",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "UNAUTHORIZED", "message": "Token invalide ou expiré"}
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "403",
            description = "Le conducteur tente de réserver son propre trajet",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "FORBIDDEN", "message": "Un conducteur ne peut pas réserver son propre trajet"}
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Trajet introuvable",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "NOT_FOUND", "message": "Trajet introuvable : id=456"}
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "409",
            description = "Plus de places disponibles sur ce trajet",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "NO_SEATS", "message": "Plus de places disponibles pour le trajet id=10"}
                    """)
            )
        )
    })
    @PostMapping
    public ResponseEntity<?> createBooking(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Identifiants de l'utilisateur et du trajet",
                required = true,
                content = @Content(
                    examples = @ExampleObject(value = """
                        {"userId": 1, "tripId": 10}
                        """)
                )
            )
            @Valid @RequestBody BookingRequest request) {

        try {
            BookingResponse response = bookingService.createBooking(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (TripNotFoundException ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "NOT_FOUND", "message", ex.getMessage()));
        } catch (ForbiddenBookingException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "FORBIDDEN", "message", ex.getMessage()));
        } catch (NoSeatsAvailableException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("error", "NO_SEATS", "message", ex.getMessage()));
        }
    }
}
