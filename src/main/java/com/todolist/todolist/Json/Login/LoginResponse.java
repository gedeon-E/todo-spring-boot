package com.todolist.todolist.Json.Login;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class LoginResponse {
    String token;
    String type = "Bearer";
    Long userId;
    String username;
    String email;
    
    public LoginResponse(String token, Long userId, String username, String email) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}

