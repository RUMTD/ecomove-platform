package com.ecomove.controller;

import com.ecomove.security.JwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller REST — Authentification et gestion des tokens JWT.
 *
 * <p>Fournit les endpoints publics (sans authentification) permettant
 * à un utilisateur d'obtenir un token JWT pour accéder à l'API.</p>
 *
 * <p>Base URL : {@code /api/v1/auth}</p>
 *
 * @author DJAKOU TCHUETKA Ruben Magdiel
 * @version 1.0.0
 * @see JwtUtils
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(
    name = "Authentification",
    description = "Login et obtention du token JWT — endpoints publics (sans Bearer)"
)
public class AuthController {

    /** Utilitaire de génération et validation des tokens JWT. */
    private final JwtUtils jwtUtils;

    /**
     * Authentifie un utilisateur et retourne un token JWT.
     *
     * <p>Le token retourné doit être inclus dans toutes les requêtes
     * suivantes via le header : {@code Authorization: Bearer <token>}</p>
     *
     * <p>Le token expire après 24 heures. Passé ce délai, l'utilisateur
     * doit se reconnecter pour obtenir un nouveau token.</p>
     *
     * @param credentials map contenant {@code email} et {@code password}
     * @return 200 avec le token JWT, ou 400 si les credentials sont invalides
     */
    @Operation(
        summary = "Se connecter",
        description = """
            Authentifie l'utilisateur et retourne un token JWT valide 24h.
            
            **Utilisation du token :**
            Cliquer sur **Authorize** en haut de la page Swagger,
            puis saisir : `Bearer <token_retourné>`
            """
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Authentification réussie — token JWT retourné",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {
                      "token": "eyJhbGciOiJIUzI1NiJ9...",
                      "type": "Bearer",
                      "email": "driver@techcorp.com",
                      "expiresIn": "24h"
                    }
                    """)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "email ou password manquant",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = """
                    {"error": "email et password requis"}
                    """)
            )
        )
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Credentials de l'utilisateur",
                required = true,
                content = @Content(
                    examples = @ExampleObject(value = """
                        {"email": "driver@techcorp.com", "password": "password123"}
                        """)
                )
            )
            @RequestBody Map<String, String> credentials) {

        String email    = credentials.get("email");
        String password = credentials.get("password");

        if (email == null || password == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "email et password requis"));
        }

        // En production : vérifier les credentials contre la BDD avec BCrypt
        String token = jwtUtils.generateToken(email);

        return ResponseEntity.ok(Map.of(
                "token",     token,
                "type",      "Bearer",
                "email",     email,
                "expiresIn", "24h"
        ));
    }
}
