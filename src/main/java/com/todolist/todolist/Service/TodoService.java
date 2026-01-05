package com.todolist.todolist.Service;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.BasicTodo;

public interface TodoService {
    public Todo createTodo(BasicTodo basicTodo);
}
