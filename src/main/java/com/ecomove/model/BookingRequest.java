package com.ecomove.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de requête — Création d'une réservation.
 *
 * <p>Représente le corps JSON attendu par {@code POST /api/v1/bookings}.
 * Toutes les contraintes de validation sont vérifiées avant d'entrer
 * dans la couche service.</p>
 *
 * <p>Exemple JSON :</p>
 * <pre>{@code
 * {
 *   "userId": 1,
 *   "tripId": 10
 * }
 * }</pre>
 *
 * @author DJAKOU TCHUETKA Ruben Magdiel
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Corps de la requête pour créer une réservation")
public class BookingRequest {

    /**
     * Identifiant de l'employé qui effectue la réservation.
     * Doit correspondre à un utilisateur existant en base de données.
     */
    @Schema(
        description = "Identifiant unique de l'utilisateur (passager)",
        example = "1",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "userId est obligatoire")
    @Positive(message = "userId doit être un entier positif")
    private Long userId;

    /**
     * Identifiant du trajet sur lequel effectuer la réservation.
     * Le trajet doit exister et avoir des places disponibles.
     */
    @Schema(
        description = "Identifiant unique du trajet à réserver",
        example = "10",
        requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull(message = "tripId est obligatoire")
    @Positive(message = "tripId doit être un entier positif")
    private Long tripId;
}
