package com.todolist.todolist.Service;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.BasicTodo;
import com.todolist.todolist.Json.UpdateTodo;

import java.util.List;

public interface TodoService {
    public Todo createTodo(BasicTodo basicTodo);
    public List<Todo> getAllTodos();
    public Todo updateTodo(Long id, UpdateTodo updateTodo);
    public void deleteTodo(Long id);
}
