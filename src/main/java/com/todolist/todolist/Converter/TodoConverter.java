package com.todolist.todolist.Converter;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.User.BasicUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Convertisseur pour transformer les entités Todo en DTOs.
 * 
 * Ce convertisseur :
 * - Masque les données sensibles de l'utilisateur propriétaire
 * - Évite les problèmes de sérialisation JSON avec les relations JPA
 * - Utilise UserConverter pour convertir le User associé
 * 
 * Workflow :
 * Todo (entité JPA avec User complet) → BasicTodo (DTO avec BasicUser)
 */
@Component
@RequiredArgsConstructor
public class TodoConverter {
    
    private final UserConverter userConverter;
    
    /**
     * Convertit une entité Todo en BasicTodo (DTO).
     * 
     * BasicTodo contient :
     * - Toutes les infos du todo (id, description, note, dates)
     * - Les infos publiques de l'utilisateur (BasicUser)
     * 
     * Informations masquées :
     * - deletedAt du todo (information interne)
     * - password de l'utilisateur (sécurité)
     * 
     * @param todo L'entité Todo à convertir
     * @return Un BasicTodo sans données sensibles
     */
    public BasicTodo convertTodoToBasicTodo(Todo todo) {
        if (todo == null) {
            return null;
        }
        
        BasicUser basicUser = userConverter.convertUserToBasicUser(todo.getUser());
        
        return new BasicTodo(
                todo.getId(),
                todo.getDescription(),
                todo.getNote(),
                todo.getFinalDate(),
                todo.getCreatedAt(),
                basicUser
        );
    }
    
    /**BasicTodo
     * Convertit une liste de Todo en liste de BasicTodo.
     * 
     * Méthode helper pour convertir facilement toute une liste.
     * Utilisée dans getAllTodos() par exemple.
     * 
     * @param todos Liste d'entités Todo
     * @return Liste de BasicTodo
     */
    public List<BasicTodo> convertTodosToBasicTodos(List<Todo> todos) {
        if (todos == null) {
            return null;
        }
        
        return todos.stream()
                .map(this::convertTodoToBasicTodo)
                .collect(Collectors.toList());
    }
}

