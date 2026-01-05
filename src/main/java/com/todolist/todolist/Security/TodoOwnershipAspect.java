package com.todolist.todolist.Security;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Repository.TodoRepository;
import com.todolist.todolist.Utils.AuthenticationUtils;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * Aspect AOP pour vérifier automatiquement la propriété des todos.
 * 
 * Cet aspect intercepte toutes les méthodes annotées avec @CheckTodoOwnership
 * et vérifie que le todo appartient bien à l'utilisateur connecté.
 * 
 * Workflow :
 * 1. Une méthode annotée @CheckTodoOwnership est appelée
 * 2. L'aspect intercepte l'appel AVANT l'exécution
 * 3. Récupère l'ID du todo depuis les paramètres de la méthode
 * 4. Charge le todo depuis la base de données
 * 5. Récupère l'utilisateur connecté
 * 6. Compare l'userId du todo avec l'userId connecté
 * 7. Si différent, lance une exception 403 Forbidden
 * 8. Si identique, laisse la méthode s'exécuter normalement
 * 
 * @Before : L'aspect s'exécute AVANT la méthode cible
 * @Around pourrait être utilisé pour plus de contrôle (before + after)
 */
@Aspect
@Component
@RequiredArgsConstructor
public class TodoOwnershipAspect {
    
    private final TodoRepository todoRepository;
    private final AuthenticationUtils authenticationUtils;
    
    /**
     * Méthode exécutée avant toute méthode annotée avec @CheckTodoOwnership.
     * 
     * Le pointcut :
     * - @annotation(...) : cible les méthodes avec cette annotation
     * - JoinPoint : donne accès aux paramètres de la méthode interceptée
     * 
     * @param joinPoint Point d'interception contenant les infos de la méthode
     * @throws ResponseStatusException 403 si le todo n'appartient pas à l'utilisateur
     * @throws ResponseStatusException 404 si le todo n'existe pas
     */
    @Before("@annotation(com.todolist.todolist.Security.CheckTodoOwnership)")
    public void checkTodoOwnership(JoinPoint joinPoint) {
        Long todoId = extractTodoId(joinPoint);
        
        if (todoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "L'ID du todo est manquant"
            );
        }
        
        Long currentUserId = authenticationUtils.getCurrentUserId();
        
        Todo todo = todoRepository.findByIdNotDeleted(todoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Todo non trouvé"
                ));
        
        if (!todo.getUser().getId().equals(currentUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Vous n'avez pas la permission d'accéder à ce todo"
            );
        }
    }
    
    /**
     * Extrait l'ID du todo depuis les paramètres de la méthode interceptée.
     * 
     * Convention : Le premier paramètre de type Long est considéré comme l'ID du todo.
     * Cela fonctionne car dans nos controllers, l'ID est toujours le premier paramètre :
     * - updateTodo(@PathVariable Long id, ...)
     * - deleteTodo(@PathVariable Long id)
     * 
     * Alternative : On pourrait chercher le paramètre annoté avec @PathVariable("id")
     * pour être plus précis, mais cette approche simple suffit ici.
     * 
     * @param joinPoint Point d'interception
     * @return L'ID du todo ou null si non trouvé
     */
    private Long extractTodoId(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        
        return null;
    }
}

