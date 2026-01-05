package com.todolist.todolist.Json;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BasicTodo {
     String description;
     String note;
     LocalDateTime finalDate;

}
