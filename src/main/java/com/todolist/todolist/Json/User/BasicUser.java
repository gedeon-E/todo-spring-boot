package com.todolist.todolist.Json.User;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BasicUser {
    Long id;
    String firstname;
    String lastname;
    String username;
    String email;
    LocalDateTime createdAt;
}

