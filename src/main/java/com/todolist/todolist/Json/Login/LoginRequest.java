package com.todolist.todolist.Json.Login;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "Le nom d'utilisateur ou email est obligatoire")
    String usernameOrEmail;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    String password;
}

