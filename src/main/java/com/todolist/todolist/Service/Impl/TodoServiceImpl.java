package com.todolist.todolist.Service.Impl;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.BasicTodo;
import com.todolist.todolist.Repository.TodoRepository;
import org.springframework.stereotype.Service;

@Service
public class TodoServiceImpl {
    private TodoRepository todoRepository;

    public Todo createTodo(BasicTodo basicTodo){
        Todo todo = new Todo();

       todo.setDescription(basicTodo.getNote());
       todo.setDescription(basicTodo.getDescription());
       todo.setFinalDate(basicTodo.getFinalDate());

        return todoRepository.save(todo);
    }


}
