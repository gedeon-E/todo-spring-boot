package com.todolist.todolist.Service;

import com.todolist.todolist.Json.Todo.CreateTodoRequest;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;

import java.util.List;

public interface TodoService {
    BasicTodo createTodo(CreateTodoRequest createTodoRequest, Long userId);
    List<BasicTodo> getAllTodosOfUser(Long userId);
    BasicTodo updateTodo(Long id, UpdateTodoRequest updateTodo);
    void deleteTodo(Long id);
}
