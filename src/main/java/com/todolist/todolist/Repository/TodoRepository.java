package com.todolist.todolist.Repository;

import com.todolist.todolist.Entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TodoRepository extends JpaRepository<Todo,Long> {
    
    @Query("SELECT t FROM Todo t WHERE t.deletedAt IS NULL")
    List<Todo> findAllNotDeleted();
    
    @Query("SELECT t FROM Todo t WHERE t.id = ?1 AND t.deletedAt IS NULL")
    Optional<Todo> findByIdNotDeleted(Long id);
    
    @Query("SELECT t FROM Todo t WHERE t.user.id = ?1 AND t.deletedAt IS NULL")
    List<Todo> findAllByUserIdNotDeleted(Long userId);
    
    @Query("SELECT t FROM Todo t WHERE t.id = ?1 AND t.user.id = ?2 AND t.deletedAt IS NULL")
    Optional<Todo> findByIdAndUserIdNotDeleted(Long id, Long userId);
}
