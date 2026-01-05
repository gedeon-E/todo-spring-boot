package com.todolist.todolist.Service;

import com.todolist.todolist.Json.Login.LoginRequest;
import com.todolist.todolist.Json.User.UpdateUserRequest;
import com.todolist.todolist.Json.User.BasicUser;
import com.todolist.todolist.Json.User.CreateUserRequest;
import com.todolist.todolist.Json.Login.LoginResponse;

import java.util.List;

public interface UserService {
    BasicUser createUser(CreateUserRequest createUserRequest);
    LoginResponse login(LoginRequest loginRequest);
    List<BasicUser> getAllUsers();
    BasicUser getUserById(Long id);
    BasicUser updateUser(Long id, UpdateUserRequest updateUserRequest);
    void deleteUser(Long id);
}

