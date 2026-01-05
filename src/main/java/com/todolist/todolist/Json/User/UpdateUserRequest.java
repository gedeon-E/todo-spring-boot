package com.todolist.todolist.Json.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    String firstname;
    
    String lastname;
    
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    String username;
    
    @Email(message = "L'email n'est pas valide")
    String email;
    
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    String password;
}

