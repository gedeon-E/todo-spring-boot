package com.todolist.todolist.Controller;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.BasicTodo;
import com.todolist.todolist.Service.TodoService;
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
    public Todo createTodo(@RequestBody BasicTodo basicTodo){
        return todoService.createTodo(basicTodo);
    }
}
