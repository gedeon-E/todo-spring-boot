package com.todolist.todolist.Json.Todo;

import com.todolist.todolist.Json.User.BasicUser;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un Todo.
 * 
 * Contient toutes les informations du todo plus les infos de base de l'utilisateur propriétaire.
 * N'expose PAS les données sensibles de User (mot de passe, etc.).
 */
@Getter
@Setter
@AllArgsConstructor
public class BasicTodo {
    Long id;
    String description;
    String note;
    LocalDateTime finalDate;
    LocalDateTime createdAt;
    BasicUser user;
}

