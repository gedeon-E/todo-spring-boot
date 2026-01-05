package com.todolist.todolist.Security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation pour vérifier automatiquement que le todo appartient à l'utilisateur connecté.
 * 
 * Cette annotation s'applique sur les méthodes de controller qui manipulent un todo.
 * Un Aspect (AOP) intercepte les méthodes annotées et vérifie la propriété avant l'exécution.
 * 
 * Utilisation :
 * @CheckTodoOwnership
 * @PutMapping("/{id}")
 * public BasicTodo updateTodo(@PathVariable Long id, @RequestBody UpdateTodoRequest request) {
 *     // La vérification est faite automatiquement avant d'entrer ici
 *     return todoService.updateTodo(id, request);
 * }
 * 
 * Fonctionnement :
 * 1. L'annotation marque les méthodes à protéger
 * 2. Un Aspect intercepte l'appel
 * 3. L'Aspect extrait l'ID du todo depuis les paramètres de la méthode
 * 4. Vérifie que le todo appartient à l'utilisateur connecté
 * 5. Si oui, continue l'exécution
 * 6. Si non, lance une exception 403 Forbidden
 * 
 * Avantages :
 * - Séparation des responsabilités (sécurité vs logique métier)
 * - Réutilisable sur plusieurs méthodes
 * - Code du controller plus propre
 * - Le service n'a plus besoin de gérer l'autorisation
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckTodoOwnership {
}

