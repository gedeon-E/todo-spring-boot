package com.todolist.todolist.Security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Configuration principale de la sécurité Spring Security.
 * 
 * Cette classe configure tous les aspects de la sécurité de l'application :
 * - Quelles routes sont publiques/protégées
 * - Comment les mots de passe sont hashés
 * - Comment l'authentification fonctionne (JWT)
 * - Ordre des filtres de sécurité
 * 
 * @Configuration : indique que cette classe contient des @Bean (composants Spring)
 * @EnableWebSecurity : active Spring Security avec cette configuration
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    
    /**
     * Configuration de la chaîne de sécurité (SecurityFilterChain).
     * 
     * C'est le cœur de la configuration Spring Security. Définit :
     * - Quelles routes nécessitent une authentification
     * - Quelles routes sont publiques
     * - Comment gérer les sessions
     * - Ordre des filtres de sécurité
     * 
     * @param http Objet de configuration fourni par Spring Security
     * @return La chaîne de sécurité configurée
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /**
                 * Désactivation de la protection CSRF (Cross-Site Request Forgery)
                 * 
                 * CSRF = attaque où un site malveillant fait faire des actions à l'utilisateur
                 * sur un autre site où il est authentifié.
                 * 
                 * On désactive car :
                 * 1. On utilise JWT (stateless), pas de cookies de session
                 * 2. Les JWT sont envoyés dans le header Authorization, pas accessible depuis un autre domaine
                 * 3. CSRF protège principalement contre les attaques basées sur les cookies
                 * 
                 * Si on utilisait des cookies pour l'authentification, il faudrait ACTIVER CSRF !
                 */
                .csrf(csrf -> csrf.disable())
                
                /**
                 * Configuration des autorisations par route
                 * 
                 * permitAll() : accessible sans authentification (routes publiques)
                 * authenticated() : nécessite d'être authentifié
                 * 
                 * IMPORTANT : Le context-path est /api (défini dans application.properties)
                 * donc Spring ajoute automatiquement /api à toutes les routes.
                 * On ne doit PAS mettre /api dans les requestMatchers, sinon ça donne /api/api/...
                 * 
                 * On spécifie explicitement la méthode HTTP pour être plus précis.
                 * 
                 * Routes publiques (POST uniquement) :
                 * - POST /users/register → accessible via http://localhost:8080/api/users/register
                 * - POST /users/login → accessible via http://localhost:8080/api/users/login
                 * 
                 * Toutes les autres routes nécessitent une authentification :
                 * - /todos (GET, POST, PUT, DELETE)
                 * - /users (GET, PUT, DELETE)
                 * 
                 * Si un utilisateur non authentifié tente d'accéder à une route protégée,
                 * il reçoit une erreur 403 Forbidden.
                 */
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/users/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/users/login").permitAll()
                        .anyRequest().authenticated()
                )
                
                /**
                 * Configuration de la gestion des sessions
                 * 
                 * STATELESS = pas de session côté serveur
                 * 
                 * Traditionnellement, Spring Security crée une session HTTP (JSESSIONID cookie)
                 * pour stocker l'utilisateur authentifié côté serveur.
                 * 
                 * Avec JWT, on n'a pas besoin de session car :
                 * - Le token contient déjà toutes les infos (username, userId, expiration)
                 * - Chaque requête est indépendante (stateless)
                 * - Le serveur ne stocke rien sur l'utilisateur entre les requêtes
                 * 
                 * Avantages du stateless :
                 * - Scalabilité : pas besoin de synchroniser les sessions entre serveurs
                 * - Simplicité : pas de gestion de session côté serveur
                 * - Performant : pas de lookup de session en base/mémoire
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                
                /**
                 * Ajout de notre filtre JWT AVANT le filtre d'authentification par défaut
                 * 
                 * Ordre d'exécution des filtres :
                 * 1. JwtAuthenticationFilter (notre filtre custom)
                 *    - Vérifie le token JWT
                 *    - Authentifie l'utilisateur si token valide
                 * 
                 * 2. UsernamePasswordAuthenticationFilter (filtre Spring Security par défaut)
                 *    - Gère l'authentification par formulaire (non utilisé ici)
                 * 
                 * 3. SecurityFilterChain
                 *    - Vérifie les autorisations (permitAll vs authenticated)
                 * 
                 * 4. Controller
                 *    - Traite la requête si autorisée
                 * 
                 * addFilterBefore() = notre filtre s'exécute en premier
                 */
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    /**
     * Bean PasswordEncoder pour hasher les mots de passe.
     * 
     * BCryptPasswordEncoder utilise l'algorithme BCrypt qui :
     * - Est lent volontairement (protège contre le brute force)
     * - Ajoute un salt automatique (deux mêmes mots de passe = deux hashs différents)
     * - Est one-way (impossible de retrouver le mot de passe depuis le hash)
     * - Est adaptatif (on peut augmenter la difficulté avec le temps)
     * 
     * Exemple :
     * password123 -> $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     * password123 -> $2a$10$8K9aQHGW.Y3C7LqV.MHeIe3TIi16c8f/xzLQR4P4fGvVRO6Z1R5je
     * (même mot de passe, hashs différents à cause du salt)
     * 
     * Utilisé pour :
     * - Hasher les mots de passe lors de la création de compte (UserServiceImpl.createUser)
     * - Vérifier les mots de passe lors du login (AuthenticationManager)
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * Bean AuthenticationManager pour gérer l'authentification.
     * 
     * L'AuthenticationManager est responsable de :
     * - Vérifier les credentials (username/password) lors du login
     * - Charger l'utilisateur avec UserDetailsService
     * - Comparer le mot de passe fourni avec le hash en base (avec PasswordEncoder)
     * 
     * Workflow du login :
     * 1. UserServiceImpl.login() appelle authenticationManager.authenticate()
     * 2. AuthenticationManager utilise UserDetailsService pour charger l'utilisateur
     * 3. Compare le mot de passe fourni avec celui en base (BCrypt)
     * 4. Si ok, retourne l'utilisateur authentifié
     * 5. Si ko, lève une BadCredentialsException (mauvais mot de passe)
     * 
     * Configuration :
     * - userDetailsService : comment charger l'utilisateur (CustomUserDetailsService)
     * - passwordEncoder : comment comparer les mots de passe (BCryptPasswordEncoder)
     * 
     * @param http Objet de configuration fourni par Spring Security
     * @return L'AuthenticationManager configuré
     */
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder = 
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}

