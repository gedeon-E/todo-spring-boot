package com.todolist.todolist.Utils;

import com.todolist.todolist.Entity.User;
import com.todolist.todolist.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * Classe utilitaire pour gérer l'authentification et récupérer l'utilisateur connecté.
 * 
 * Cette classe centralise la logique de récupération de l'utilisateur authentifié
 * pour éviter de dupliquer ce code dans chaque controller.
 * 
 * Workflow :
 * 1. Récupère l'objet Authentication depuis le SecurityContext
 * 2. Extrait le username de l'utilisateur authentifié
 * 3. Charge l'utilisateur complet depuis la base de données
 * 4. Retourne l'ID ou l'entité User complète
 */
@Component
@RequiredArgsConstructor
public class AuthenticationUtils {
    
    private final UserRepository userRepository;
    
    /**
     * Récupère l'ID de l'utilisateur actuellement connecté.
     * 
     * Cette méthode est utilisée dans les controllers pour :
     * - Associer un todo à l'utilisateur qui le crée
     * - Filtrer les todos par utilisateur
     * - Vérifier les permissions (modification/suppression)
     * 
     * @return L'ID de l'utilisateur connecté
     * @throws RuntimeException si l'utilisateur n'est pas trouvé (user supprimé ou token invalide)
     * 
     * Note : Cette méthode suppose qu'un utilisateur est authentifié.
     * Les routes protégées par Spring Security garantissent cela.
     */
    public Long getCurrentUserId() {
        String username = getCurrentUsername();
        
        User user = userRepository.findByUsernameNotDeleted(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        return user.getId();
    }
    
    /**
     * Récupère l'entité User complète de l'utilisateur actuellement connecté.
     * 
     * Utile quand on a besoin de plus d'informations que juste l'ID
     * (nom, prénom, email, etc.)
     * 
     * @return L'entité User complète
     * @throws RuntimeException si l'utilisateur n'est pas trouvé
     */
    public User getCurrentUser() {
        String username = getCurrentUsername();
        
        return userRepository.findByUsernameNotDeleted(username)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
    }
    
    /**
     * Récupère le username de l'utilisateur actuellement connecté.
     * 
     * Le username est stocké dans le SecurityContext par le JwtAuthenticationFilter
     * lors de la validation du token JWT.
     * 
     * @return Le username de l'utilisateur connecté
     * 
     * Workflow détaillé :
     * 1. SecurityContextHolder contient le contexte de sécurité (thread-safe)
     * 2. getAuthentication() retourne l'objet Authentication (mis par JwtAuthenticationFilter)
     * 3. getName() retourne le username (principal)
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName();
    }
    
    /**
     * Vérifie si un todo appartient à l'utilisateur connecté.
     * 
     * Méthode helper pour valider les permissions.
     * 
     * @param ownerId L'ID du propriétaire du todo
     * @return true si le todo appartient à l'utilisateur connecté
     */
    public boolean isCurrentUser(Long ownerId) {
        return getCurrentUserId().equals(ownerId);
    }
}

