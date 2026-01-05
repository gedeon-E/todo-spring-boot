package com.todolist.todolist.Exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour l'application.
 * 
 * @RestControllerAdvice : intercepte toutes les exceptions lancées par les controllers
 * et permet de retourner des réponses JSON formatées au lieu des erreurs par défaut.
 * 
 * Avantages :
 * - Messages d'erreur clairs et structurés pour le client
 * - Codes HTTP appropriés selon le type d'erreur
 * - Centralisation de la gestion des erreurs
 * - Évite de dupliquer les try-catch dans chaque controller
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Gère les erreurs de validation des @RequestBody avec @Valid.
     * 
     * Lancée quand :
     * - @NotBlank échoue (champ vide)
     * - @Size échoue (taille min/max non respectée)
     * - @Email échoue (email invalide)
     * - etc.
     * 
     * Exemple : mot de passe de 5 caractères alors que @Size(min = 6)
     * 
     * @param ex L'exception contenant toutes les erreurs de validation
     * @return 400 Bad Request avec la liste détaillée des erreurs par champ
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Erreur de validation: {}", errors);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur de validation",
                errors,
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère les erreurs d'authentification (mauvais credentials).
     * 
     * Lancée lors du login quand :
     * - Le username/email n'existe pas
     * - Le mot de passe est incorrect
     * 
     * @param ex L'exception d'authentification
     * @return 401 Unauthorized avec un message d'erreur
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Tentative de connexion échouée: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.UNAUTHORIZED.value(),
                "Identifiants incorrects",
                Map.of("error", "Le nom d'utilisateur/email ou le mot de passe est incorrect"),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Gère les erreurs quand un utilisateur n'est pas trouvé.
     * 
     * Lancée quand :
     * - L'utilisateur n'existe pas en base de données
     * - L'utilisateur a été supprimé (soft delete)
     * 
     * @param ex L'exception contenant le message d'erreur
     * @return 404 Not Found avec un message d'erreur
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UsernameNotFoundException ex) {
        logger.warn("Utilisateur non trouvé: {}", ex.getMessage());
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                "Utilisateur non trouvé",
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Gère toutes les RuntimeException génériques (erreurs métier).
     * 
     * Lancée pour :
     * - Username déjà existant
     * - Email déjà existant
     * - Todo non trouvé
     * - Utilisateur déjà supprimé
     * - etc.
     * 
     * @param ex L'exception contenant le message d'erreur
     * @return 400 Bad Request avec le message d'erreur
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        logger.error("RuntimeException: {}", ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Erreur",
                Map.of("error", ex.getMessage()),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    /**
     * Gère toutes les autres exceptions non prévues.
     * 
     * Filet de sécurité pour éviter d'exposer des stacktraces au client.
     * En production, on ne doit jamais montrer les détails internes de l'erreur.
     * 
     * @param ex L'exception générique
     * @return 500 Internal Server Error avec un message générique
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        logger.error("Exception non gérée: {} - {}", ex.getClass().getName(), ex.getMessage(), ex);
        
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erreur interne du serveur",
                Map.of("error", "Une erreur inattendue s'est produite", "details", ex.getMessage()),
                LocalDateTime.now()
        );
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Classe pour structurer les réponses d'erreur en JSON.
     * 
     * Format de réponse :
     * {
     *   "status": 400,
     *   "message": "Erreur de validation",
     *   "errors": {
     *     "password": "Le mot de passe doit contenir au moins 6 caractères",
     *     "email": "L'email n'est pas valide"
     *   },
     *   "timestamp": "2026-01-05T18:30:00"
     * }
     */
    public static class ErrorResponse {
        private int status;
        private String message;
        private Map<String, String> errors;
        private LocalDateTime timestamp;

        public ErrorResponse(int status, String message, Map<String, String> errors, LocalDateTime timestamp) {
            this.status = status;
            this.message = message;
            this.errors = errors;
            this.timestamp = timestamp;
        }

        public int getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public Map<String, String> getErrors() {
            return errors;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}

