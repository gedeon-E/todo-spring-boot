package com.todolist.todolist.Repository;

import com.todolist.todolist.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    List<User> findAllNotDeleted();
    
    @Query("SELECT u FROM User u WHERE u.id = ?1 AND u.deletedAt IS NULL")
    Optional<User> findByIdNotDeleted(Long id);
    
    @Query("SELECT u FROM User u WHERE u.username = ?1 AND u.deletedAt IS NULL")
    Optional<User> findByUsernameNotDeleted(String username);
    
    @Query("SELECT u FROM User u WHERE u.email = ?1 AND u.deletedAt IS NULL")
    Optional<User> findByEmailNotDeleted(String email);
    
    @Query("SELECT u FROM User u WHERE (u.username = ?1 OR u.email = ?1) AND u.deletedAt IS NULL")
    Optional<User> findByUsernameOrEmailNotDeleted(String usernameOrEmail);
}

