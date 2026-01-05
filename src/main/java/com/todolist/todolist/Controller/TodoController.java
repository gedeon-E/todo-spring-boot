package com.todolist.todolist.Controller;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.BasicTodo;
import com.todolist.todolist.Repository.TodoRepository;
import com.todolist.todolist.Service.Impl.TodoServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("todos")
@RequiredArgsConstructor
public class TodoController {
    private TodoServiceImpl todoServiceImpl;

    @GetMapping("/")
    public String getTodoList(){
        return "Hello every body";
    }

    @PostMapping("/")
    public Todo createTodoList(@RequestBody BasicTodo basicTodo){
        return  todoServiceImpl.createTodo(basicTodo);

    }
}
