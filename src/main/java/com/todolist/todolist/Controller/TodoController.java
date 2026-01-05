package com.todolist.todolist.Controller;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;
import com.todolist.todolist.Service.TodoService;
import com.todolist.todolist.Utils.AuthenticationUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("todos")
@RequiredArgsConstructor
public class TodoController {
    
    private final TodoService todoService;
    private final AuthenticationUtils authenticationUtils;

    @GetMapping
    public List<Todo> getTodoList(){
        Long userId = authenticationUtils.getCurrentUserId();
        return todoService.getAllTodosOfUser(userId);
    }

    @PostMapping
    public Todo createTodo(@Valid @RequestBody BasicTodo basicTodo){
        Long userId = authenticationUtils.getCurrentUserId();
        return todoService.createTodo(basicTodo, userId);
    }

    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody UpdateTodoRequest updateTodo){
        Long userId = authenticationUtils.getCurrentUserId();
        return todoService.updateTodo(id, updateTodo, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id){
        Long userId = authenticationUtils.getCurrentUserId();
        todoService.deleteTodo(id, userId);
    }
}
