package com.ecomove.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Configuration OpenAPI 3.0 — Documentation interactive de l'API EcoMove.
 *
 * <p>Génère automatiquement la documentation depuis les annotations
 * présentes dans les controllers et les DTOs.</p>
 *
 * <p>Accès Swagger UI : <a href="http://localhost:8080/swagger-ui.html">
 * http://localhost:8080/swagger-ui.html</a></p>
 *
 * <p>Accès JSON brut : <a href="http://localhost:8080/v3/api-docs">
 * http://localhost:8080/v3/api-docs</a></p>
 *
 * @author DJAKOU TCHUETKA Ruben Magdiel
 * @version 1.0.0
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure la spécification OpenAPI de l'API EcoMove.
     *
     * <p>Définit :</p>
     * <ul>
     *   <li>Les métadonnées du projet (titre, version, contact)</li>
     *   <li>L'authentification JWT Bearer pour Swagger UI</li>
     *   <li>Les environnements disponibles (local, staging)</li>
     * </ul>
     *
     * @return l'objet OpenAPI configuré
     */
    @Bean
    public OpenAPI ecomoveOpenAPI() {

        // Nom du schéma de sécurité — référencé dans les controllers
        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()
            // ── Informations générales ──────────────────────────────────
            .info(new Info()
                .title("EcoMove API")
                .description("""
                    ## Plateforme B2B de Covoiturage Domicile-Travail
                    
                    API REST permettant :
                    - La **gestion des trajets** professionnels entre entreprises partenaires
                    - La **réservation** de places pour les employés
                    - Le **calcul des économies CO2** générées par le covoiturage
                    - L'**authentification sécurisée** via JWT
                    
                    ### Authentification
                    1. Appeler `POST /api/v1/auth/login` avec vos credentials
                    2. Copier le `token` retourné
                    3. Cliquer sur **Authorize** et saisir : `Bearer <votre_token>`
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("DJAKOU TCHUETKA Ruben Magdiel")
                    .email("ruben.djakou@ecomove.io"))
                .license(new License()
                    .name("Propriétaire — Institut 3iAC")))

            // ── Serveurs disponibles ────────────────────────────────────
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Développement local"),
                new Server().url("https://staging.ecomove.io").description("Staging")
            ))

            // ── Sécurité JWT — apparaît dans le bouton "Authorize" ─────
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(new Components()
                .addSecuritySchemes(securitySchemeName,
                    new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Token JWT obtenu via POST /api/v1/auth/login")
                ));
    }
}
