package com.todolist.todolist.Converter;

import com.todolist.todolist.Entity.User;
import com.todolist.todolist.Json.User.BasicUser;
import org.springframework.stereotype.Component;

/**
 * Convertisseur pour transformer les entités User en DTOs.
 * 
 * Ce convertisseur permet de :
 * - Masquer les données sensibles (mot de passe, deletedAt, etc.)
 * - Contrôler exactement quelles informations sont exposées au client
 * - Éviter les boucles infinies de sérialisation JSON (User -> Todo -> User)
 * 
 * Principe : Ne jamais exposer directement les entités JPA au client.
 * Toujours passer par des DTOs (Data Transfer Objects).
 */
@Component
public class UserConverter {
    
    /**
     * Convertit une entité User en BasicUser (DTO).
     * 
     * BasicUser contient uniquement les informations publiques :
     * - id, firstname, lastname, username, email, createdAt
     * 
     * Informations masquées :
     * - password (sécurité)
     * - deletedAt (information interne)
     * 
     * @param user L'entité User à convertir
     * @return Un BasicUser sans données sensibles
     */
    public BasicUser convertUserToBasicUser(User user) {
        if (user == null) {
            return null;
        }
        
        BasicUser basicUser = new BasicUser();
        basicUser.setId(user.getId());
        basicUser.setFirstname(user.getFirstname());
        basicUser.setLastname(user.getLastname());
        basicUser.setUsername(user.getUsername());
        basicUser.setEmail(user.getEmail());
        basicUser.setCreatedAt(user.getCreatedAt());
        return basicUser;

    }
}
