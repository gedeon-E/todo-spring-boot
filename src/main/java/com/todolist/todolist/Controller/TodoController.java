package com.todolist.todolist.Controller;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;
import com.todolist.todolist.Service.TodoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("todos")
public class TodoController {
    @Autowired
    private TodoService todoService;

    @GetMapping
    public List<Todo> getTodoList(){
        return todoService.getAllTodos();
    }

    @PostMapping
    public Todo createTodo(@Valid @RequestBody BasicTodo basicTodo){
        return todoService.createTodo(basicTodo);
    }

    @PutMapping("/{id}")
    public Todo updateTodo(@PathVariable Long id, @RequestBody UpdateTodoRequest updateTodo){
        return todoService.updateTodo(id, updateTodo);
    }

    @DeleteMapping("/{id}")
    public void deleteTodo(@PathVariable Long id){
        todoService.deleteTodo(id);
    }
}
