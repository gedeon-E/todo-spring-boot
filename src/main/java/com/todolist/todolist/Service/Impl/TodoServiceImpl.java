package com.todolist.todolist.Service.Impl;

import com.todolist.todolist.Converter.TodoConverter;
import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Entity.User;
import com.todolist.todolist.Json.Todo.CreateTodoRequest;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;
import com.todolist.todolist.Repository.TodoRepository;
import com.todolist.todolist.Repository.UserRepository;
import com.todolist.todolist.Service.TodoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    
    private final TodoRepository todoRepository;
    private final UserRepository userRepository;
    private final TodoConverter todoConverter;

    @Override
    public BasicTodo createTodo(CreateTodoRequest createTodoRequest, Long userId){
        User user = userRepository.findByIdNotDeleted(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
        
        Todo todo = new Todo();
        todo.setNote(createTodoRequest.getNote());
        todo.setDescription(createTodoRequest.getDescription());
        todo.setFinalDate(createTodoRequest.getFinalDate());
        todo.setUser(user);

        Todo savedTodo = todoRepository.save(todo);
        
        return todoConverter.convertTodoToBasicTodo(savedTodo);
    }

    @Override
    public List<BasicTodo> getAllTodosOfUser(Long userId){
        List<Todo> todos = todoRepository.findAllByUserIdNotDeleted(userId);
        return todoConverter.convertTodosToBasicTodos(todos);
    }

    @Override
    public BasicTodo updateTodo(Long id, UpdateTodoRequest updateTodo, Long userId){
        Todo todo = todoRepository.findByIdAndUserIdNotDeleted(id, userId)
                .orElseThrow(() -> new RuntimeException("Todo non trouvé ou vous n'avez pas la permission de le modifier"));
        
        if (updateTodo.getNote() != null) {
            todo.setNote(updateTodo.getNote());
        }
        if (updateTodo.getDescription() != null) {
            todo.setDescription(updateTodo.getDescription());
        }
        if (updateTodo.getFinalDate() != null) {
            todo.setFinalDate(updateTodo.getFinalDate());
        }
        
        Todo updatedTodo = todoRepository.save(todo);
        
        return todoConverter.convertTodoToBasicTodo(updatedTodo);
    }

    @Override
    public void deleteTodo(Long id, Long userId){
        Todo todo = todoRepository.findByIdAndUserIdNotDeleted(id, userId)
                .orElseThrow(() -> new RuntimeException("Todo non trouvé ou vous n'avez pas la permission de le supprimer"));
        
        todo.setDeletedAt(java.time.LocalDateTime.now());
        todoRepository.save(todo);
    }

}
