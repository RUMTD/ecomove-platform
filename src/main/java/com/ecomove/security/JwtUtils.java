package com.ecomove.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * Utilitaire JWT — Génération et validation des tokens
 * Utilisé par le filtre de sécurité pour authentifier les requêtes API
 */
@Component
public class JwtUtils {

    @Value("${jwt.secret:ecomove_jwt_secret_key_min_256_bits_pour_hs256_algorithm}")
    private String jwtSecret;

    @Value("${jwt.expiration.ms:86400000}")   // 24h par défaut
    private int jwtExpirationMs;

    /**
     * Génère un token JWT pour un utilisateur authentifié
     */
    public String generateToken(String email) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extrait l'email (subject) du token
     */
    public String getEmailFromToken(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * Valide le token : signature + expiration
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims parseClaims(String token) {
        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
