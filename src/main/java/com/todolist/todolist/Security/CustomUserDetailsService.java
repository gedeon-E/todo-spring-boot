package com.todolist.todolist.Security;

import com.todolist.todolist.Entity.User;
import com.todolist.todolist.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

/**
 * Service personnalisé pour charger les utilisateurs depuis la base de données.
 * 
 * Cette classe implémente UserDetailsService, l'interface standard de Spring Security
 * pour récupérer les informations d'un utilisateur.
 * 
 * Spring Security utilise ce service pour :
 * 1. Vérifier les credentials lors du login (AuthenticationManager)
 * 2. Charger l'utilisateur lors de la validation du JWT (JwtAuthenticationFilter)
 * 
 * Pourquoi "Custom" ?
 * Spring Security ne sait pas comment nos utilisateurs sont stockés (base de données, LDAP, etc.).
 * On doit donc lui dire comment récupérer un utilisateur en implémentant UserDetailsService.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    /**
     * Charge un utilisateur depuis la base de données par son username ou email.
     * 
     * Cette méthode est appelée par Spring Security dans deux contextes :
     * 
     * 1. Lors du login (POST /api/users/login) :
     *    - AuthenticationManager appelle cette méthode
     *    - Récupère l'utilisateur et compare le mot de passe hashé
     *    - Si ok, génère un token JWT
     * 
     * 2. Lors de chaque requête authentifiée :
     *    - JwtAuthenticationFilter appelle cette méthode
     *    - Vérifie que l'utilisateur dans le token existe toujours en base
     *    - Charge ses permissions/rôles pour vérifier les autorisations
     * 
     * @param usernameOrEmail Le username ou email de l'utilisateur (flexible)
     * @return UserDetails Un objet contenant les infos de l'utilisateur (username, password, roles)
     * @throws UsernameNotFoundException Si l'utilisateur n'existe pas ou est supprimé (soft delete)
     * 
     * Gestion du soft delete :
     * On utilise findByUsernameOrEmailNotDeleted() pour exclure les utilisateurs supprimés.
     * Un utilisateur supprimé ne peut plus se connecter même avec un token valide.
     */
    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        /**
         * Étape 1 : Recherche de l'utilisateur en base de données
         * La recherche se fait par username OU email (flexible pour le login)
         * On exclut les utilisateurs supprimés (deletedAt != null)
         */
        User user = userRepository.findByUsernameOrEmailNotDeleted(usernameOrEmail)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé: " + usernameOrEmail));
        
        /**
         * Étape 2 : Conversion de notre entité User vers UserDetails (interface Spring Security)
         * 
         * org.springframework.security.core.userdetails.User est l'implémentation par défaut
         * de UserDetails fournie par Spring Security.
         * 
         * Paramètres :
         * - username : l'identifiant unique de l'utilisateur
         * - password : le mot de passe hashé (BCrypt) stocké en base
         * - authorities : la liste des rôles/permissions (vide pour l'instant = new ArrayList<>())
         * 
         * Note sur les authorities :
         * new ArrayList<>() = aucun rôle spécifique (tous les utilisateurs authentifiés ont les mêmes droits)
         * 
         * Pour ajouter des rôles plus tard :
         * List<GrantedAuthority> authorities = new ArrayList<>();
         * authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
         * authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
         * 
         * Puis dans SecurityConfig, on pourrait faire :
         * .requestMatchers("/api/admin/**").hasRole("ADMIN")
         */
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                new ArrayList<>()
        );
    }
}

