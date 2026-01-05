package com.todolist.todolist.Service.Impl;

import com.todolist.todolist.Entity.User;
import com.todolist.todolist.Json.Login.LoginRequest;
import com.todolist.todolist.Json.User.UpdateUserRequest;
import com.todolist.todolist.Json.User.BasicUser;
import com.todolist.todolist.Json.User.CreateUserRequest;
import com.todolist.todolist.Json.Login.LoginResponse;
import com.todolist.todolist.Repository.UserRepository;
import com.todolist.todolist.Security.JwtUtil;
import com.todolist.todolist.Service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    @Override
    public BasicUser createUser(CreateUserRequest request) {
        if (userRepository.findByUsernameNotDeleted(request.getUsername()).isPresent()) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }

        if (userRepository.findByEmailNotDeleted(request.getEmail()).isPresent()) {
            throw new RuntimeException("L'email existe déjà");
        }

        User user = new User();
        user.setFirstname(request.getFirstname());
        user.setLastname(request.getLastname());
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsernameOrEmail(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsernameOrEmailNotDeleted(loginRequest.getUsernameOrEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        String token = jwtUtil.generateToken(user.getUsername(), user.getId());

        return new LoginResponse(token, user.getId(), user.getUsername(), user.getEmail());
    }

    @Override
    public List<BasicUser> getAllUsers() {
        return userRepository.findAllNotDeleted()
                .stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public BasicUser getUserById(Long id) {
        User user = userRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        return mapToUserResponse(user);
    }

    @Override
    public BasicUser updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        if (request.getFirstname() != null) {
            user.setFirstname(request.getFirstname());
        }

        if (request.getLastname() != null) {
            user.setLastname(request.getLastname());
        }

        if (request.getUsername() != null) {
            if (userRepository.findByUsernameNotDeleted(request.getUsername()).isPresent() &&
                    !user.getUsername().equals(request.getUsername())) {
                throw new RuntimeException("Le nom d'utilisateur existe déjà");
            }
            user.setUsername(request.getUsername());
        }

        if (request.getEmail() != null) {
            if (userRepository.findByEmailNotDeleted(request.getEmail()).isPresent() &&
                    !user.getEmail().equals(request.getEmail())) {
                throw new RuntimeException("L'email existe déjà");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getPassword() != null) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Override
    public void deleteUser(Long id) {
        User user = userRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
    }

    private BasicUser mapToUserResponse(User user) {
        return new BasicUser(
                user.getId(),
                user.getFirstname(),
                user.getLastname(),
                user.getUsername(),
                user.getEmail(),
                user.getCreatedAt()
        );
    }
}

