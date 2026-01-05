package com.todolist.todolist.Service;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;

import java.util.List;

public interface TodoService {
    Todo createTodo(BasicTodo basicTodo, Long userId);
    List<Todo> getAllTodosOfUser(Long userId);
    Todo updateTodo(Long id, UpdateTodoRequest updateTodo, Long userId);
    void deleteTodo(Long id, Long userId);
}
