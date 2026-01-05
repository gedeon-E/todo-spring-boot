package com.todolist.todolist.Service;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;

import java.util.List;

public interface TodoService {
    Todo createTodo(BasicTodo basicTodo);
    List<Todo> getAllTodos();
    Todo updateTodo(Long id, UpdateTodoRequest updateTodo);
    void deleteTodo(Long id);
}
