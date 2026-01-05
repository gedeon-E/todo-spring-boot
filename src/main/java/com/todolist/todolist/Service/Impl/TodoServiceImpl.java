package com.todolist.todolist.Service.Impl;

import com.todolist.todolist.Entity.Todo;
import com.todolist.todolist.Json.Todo.BasicTodo;
import com.todolist.todolist.Json.Todo.UpdateTodoRequest;
import com.todolist.todolist.Repository.TodoRepository;
import com.todolist.todolist.Service.TodoService;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TodoServiceImpl implements TodoService {
    @Autowired
    private TodoRepository todoRepository;

    public Todo createTodo(BasicTodo basicTodo){
        Todo todo = new Todo();

        todo.setNote(basicTodo.getNote());
        todo.setDescription(basicTodo.getDescription());
        todo.setFinalDate(basicTodo.getFinalDate());

        return todoRepository.save(todo);
    }

    public List<Todo> getAllTodos(){
        return todoRepository.findAllNotDeleted();
    }

    public Todo updateTodo(Long id, UpdateTodoRequest updateTodo){
        Todo todo = todoRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Todo non trouvé ou déjà supprimé"));
        
        if (updateTodo.getNote() != null) {
            todo.setNote(updateTodo.getNote());
        }
        if (updateTodo.getDescription() != null) {
            todo.setDescription(updateTodo.getDescription());
        }
        if (updateTodo.getFinalDate() != null) {
            todo.setFinalDate(updateTodo.getFinalDate());
        }
        
        return todoRepository.save(todo);
    }

    public void deleteTodo(Long id){
        Todo todo = todoRepository.findByIdNotDeleted(id)
                .orElseThrow(() -> new RuntimeException("Todo non trouvé ou déjà supprimé"));
        
        todo.setDeletedAt(java.time.LocalDateTime.now());
        todoRepository.save(todo);
    }

}
