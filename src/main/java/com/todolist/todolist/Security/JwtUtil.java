package com.todolist.todolist.Security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * Utilitaire pour la gestion des tokens JWT (JSON Web Token).
 * 
 * Un JWT est un token sécurisé qui contient des informations sur l'utilisateur (claims).
 * Il est composé de 3 parties séparées par des points :
 * - Header : algorithme de signature (HS512)
 * - Payload : données (username, userId, dates)
 * - Signature : garantit que le token n'a pas été modifié
 * 
 * Workflow :
 * 1. Lors du login, on génère un token JWT avec generateToken()
 * 2. Le client stocke ce token et l'envoie dans chaque requête (Header Authorization)
 * 3. Le serveur valide le token avec validateToken() avant d'autoriser l'accès
 */
@Component
public class JwtUtil {
    
    /**
     * Clé secrète pour signer les tokens JWT.
     * Cette clé doit être gardée secrète et ne jamais être exposée.
     * Elle utilise l'algorithme HMAC-SHA512 (HS512) qui nécessite minimum 512 bits (64 bytes).
     * 
     * IMPORTANT : En production, cette clé doit être stockée dans une variable d'environnement
     * ou un gestionnaire de secrets (AWS Secrets Manager, HashiCorp Vault, etc.)
     * et non en dur dans le code.
     */
    private final SecretKey secretKey = Keys.hmacShaKeyFor(
            "your-secret-key-must-be-at-least-256-bits-long-for-hs512-algorithm".getBytes()
    );
    
    /**
     * Durée de validité du token en millisecondes.
     * Valeur par défaut : 86400000 ms = 24 heures
     * Peut être configurée dans application.properties avec jwt.expiration
     * 
     * Plus la durée est courte, plus c'est sécurisé mais moins pratique pour l'utilisateur.
     * Plus la durée est longue, plus c'est pratique mais moins sécurisé.
     */
    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;
    
    /**
     * Génère un nouveau token JWT pour un utilisateur.
     * 
     * Le token contient :
     * - subject : le username (identifiant principal)
     * - claim "userId" : l'ID de l'utilisateur en base de données
     * - issuedAt : date de création du token
     * - expiration : date d'expiration (maintenant + jwtExpiration)
     * 
     * @param username Le nom d'utilisateur
     * @param userId L'ID de l'utilisateur en base de données
     * @return Un token JWT signé sous forme de String (format : xxxxx.yyyyy.zzzzz)
     */
    public String generateToken(String username, Long userId) {
        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(secretKey)
                .compact();
    }
    
    /**
     * Extrait le username du token JWT.
     * Le username est stocké dans le "subject" du token.
     * 
     * @param token Le token JWT
     * @return Le username de l'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }
    
    /**
     * Extrait l'ID utilisateur du token JWT.
     * L'userId est stocké dans un claim custom nommé "userId".
     * 
     * @param token Le token JWT
     * @return L'ID de l'utilisateur
     */
    public Long extractUserId(String token) {
        return extractClaims(token).get("userId", Long.class);
    }
    
    /**
     * Valide un token JWT en vérifiant :
     * 1. Que le username dans le token correspond au username fourni
     * 2. Que le token n'a pas expiré
     * 
     * Cette méthode ne lève pas d'exception si le token est invalide, elle retourne false.
     * Les exceptions de parsing sont gérées par extractClaims().
     * 
     * @param token Le token JWT à valider
     * @param username Le username attendu
     * @return true si le token est valide, false sinon
     */
    public boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
    
    /**
     * Parse le token JWT et extrait tous les claims (données contenues dans le token).
     * 
     * Claims = données stockées dans le payload du JWT (username, userId, dates, etc.)
     * 
     * Cette méthode :
     * - Vérifie la signature du token avec la clé secrète
     * - Parse le token en objet Claims
     * - Lève une exception si le token est invalide ou la signature incorrecte
     * 
     * @param token Le token JWT
     * @return Un objet Claims contenant toutes les données du token
     * @throws io.jsonwebtoken.JwtException si le token est invalide
     */
    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Vérifie si le token a expiré.
     * Compare la date d'expiration du token avec la date actuelle.
     * 
     * @param token Le token JWT
     * @return true si le token est expiré, false sinon
     */
    private boolean isTokenExpired(String token) {
        return extractClaims(token).getExpiration().before(new Date());
    }
}

