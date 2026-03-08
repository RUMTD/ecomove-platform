package com.ecomove.config;

import com.ecomove.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration Spring Security — Architecture Stateless JWT
 *
 * Règles d'accès :
 *   PUBLIC  : POST /api/v1/auth/**    (login, register)
 *   PUBLIC  : GET  /actuator/health   (monitoring)
 *   PUBLIC  : /h2-console/**          (dev uniquement)
 *   PROTÉGÉ : tout le reste → JWT requis
 */
@Configuration
@EnableWebSecurity
@Profile("!test")
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Désactiver CSRF (API REST stateless, pas de session)
            .csrf(AbstractHttpConfigurer::disable)

            // Pas de session HTTP — chaque requête est authentifiée par JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Règles d'autorisation par endpoint
            .authorizeHttpRequests(auth -> auth
                // Authentification : accessible sans token
                .requestMatchers("/api/v1/auth/**").permitAll()
                // Monitoring : accessible sans token
                .requestMatchers("/actuator/health").permitAll()
                // H2 Console : dev uniquement (désactiver en production)
                .requestMatchers("/h2-console/**").permitAll()
                // Swagger UI
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Tout le reste → JWT obligatoire
                .anyRequest().authenticated()
            )

            // Ajouter le filtre JWT AVANT le filtre d'authentification standard
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Encoder les mots de passe en BCrypt (force 10 = standard industrie)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}
