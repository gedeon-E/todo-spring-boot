package com.todolist.todolist.Security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre d'authentification JWT qui intercepte TOUTES les requêtes HTTP.
 * 
 * Ce filtre s'exécute AVANT les controllers pour vérifier si l'utilisateur est authentifié.
 * Il hérite de OncePerRequestFilter pour garantir qu'il s'exécute une seule fois par requête.
 * 
 * Fonctionnement :
 * 1. Intercepte chaque requête HTTP entrante
 * 2. Vérifie si un token JWT est présent dans le header "Authorization"
 * 3. Si oui, valide le token et authentifie l'utilisateur dans le SecurityContext
 * 4. Si non, laisse passer (les routes publiques sont gérées par SecurityConfig)
 * 5. Passe la requête au filtre suivant dans la chaîne
 * 
 * Ordre d'exécution : JwtAuthenticationFilter -> UsernamePasswordAuthenticationFilter -> Controllers
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    /**
     * Méthode principale du filtre, appelée pour chaque requête HTTP.
     * 
     * Workflow détaillé :
     * 1. Récupère le header "Authorization" (format attendu : "Bearer <token>")
     * 2. Si le header est absent ou mal formaté, passe au filtre suivant (route peut être publique)
     * 3. Extrait le token JWT (enlève le préfixe "Bearer ")
     * 4. Extrait le username du token
     * 5. Charge les informations de l'utilisateur depuis la base de données
     * 6. Valide le token (signature + expiration + correspondance username)
     * 7. Si valide, crée un objet Authentication et le stocke dans le SecurityContext
     * 8. SecurityContext = contexte Spring Security qui contient l'utilisateur authentifié
     * 9. Les controllers peuvent ensuite accéder à cet utilisateur avec @AuthenticationPrincipal
     * 
     * @param request La requête HTTP entrante
     * @param response La réponse HTTP sortante
     * @param filterChain La chaîne de filtres à exécuter après celui-ci
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        
        /**
         * Étape 1 : Récupération du header Authorization
         * Format attendu : "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9..."
         */
        final String authHeader = request.getHeader("Authorization");
        
        /**
         * Étape 2 : Vérification du format du header
         * Si pas de header ou si ne commence pas par "Bearer ", on laisse passer.
         * La requête peut être pour une route publique (register, login).
         * SecurityConfig bloquera l'accès aux routes protégées si pas authentifié.
         */
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            /**
             * Étape 3 : Extraction du token JWT
             * On enlève le préfixe "Bearer " (7 caractères) pour obtenir le token pur.
             */
            final String jwt = authHeader.substring(7);
            
            /**
             * Étape 4 : Extraction du username depuis le token
             * Le username est stocké dans le "subject" du JWT.
             */
            final String username = jwtUtil.extractUsername(jwt);
            
            /**
             * Étape 5 : Vérification si l'utilisateur n'est pas déjà authentifié
             * username != null : le token contient bien un username
             * SecurityContextHolder.getContext().getAuthentication() == null : pas encore authentifié
             * 
             * SecurityContextHolder = conteneur global qui stocke l'utilisateur authentifié
             * pour la requête courante. Il est thread-safe (un contexte par thread/requête).
             */
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                /**
                 * Étape 6 : Chargement des détails de l'utilisateur depuis la base de données
                 * UserDetailsService fait la requête SQL pour récupérer l'utilisateur.
                 * UserDetails contient : username, password (hashé), roles/permissions.
                 */
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                
                /**
                 * Étape 7 : Validation du token JWT
                 * Vérifie que :
                 * - Le username dans le token correspond à celui chargé
                 * - Le token n'a pas expiré
                 * - La signature est valide
                 */
                if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                    /**
                     * Étape 8 : Création de l'objet Authentication
                     * UsernamePasswordAuthenticationToken = objet qui représente un utilisateur authentifié
                     * Paramètres :
                     * - principal : l'utilisateur (UserDetails)
                     * - credentials : les credentials (null car déjà authentifié par JWT)
                     * - authorities : les rôles/permissions de l'utilisateur
                     */
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    
                    /**
                     * Ajout des détails de la requête (IP, session, etc.)
                     * Utile pour l'audit et la traçabilité.
                     */
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    /**
                     * Étape 9 : Stockage de l'authentification dans le SecurityContext
                     * À partir de maintenant, Spring Security considère l'utilisateur comme authentifié.
                     * Les controllers peuvent accéder à l'utilisateur avec @AuthenticationPrincipal.
                     */
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            /**
             * En cas d'erreur (token invalide, expiré, malformé, utilisateur supprimé, etc.),
             * on log l'erreur mais on laisse continuer.
             * SecurityConfig bloquera l'accès si l'utilisateur n'est pas authentifié.
             */
            logger.error("Cannot set user authentication: {}", e);
        }
        
        /**
         * Étape 10 : Passage au filtre suivant dans la chaîne
         * La requête continue son chemin vers le controller.
         * Si l'utilisateur est authentifié, il peut accéder aux routes protégées.
         * Sinon, SecurityConfig renverra 403 Forbidden.
         */
        filterChain.doFilter(request, response);
    }
}

