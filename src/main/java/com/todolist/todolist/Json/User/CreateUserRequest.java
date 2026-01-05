package com.todolist.todolist.Json.User;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateUserRequest {
    @NotBlank(message = "Le prénom est obligatoire")
    String firstname;
    
    @NotBlank(message = "Le nom est obligatoire")
    String lastname;
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    @Size(min = 3, max = 50, message = "Le nom d'utilisateur doit contenir entre 3 et 50 caractères")
    String username;
    
    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "L'email n'est pas valide")
    String email;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères")
    String password;
}

