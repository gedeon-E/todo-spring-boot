package com.todolist.todolist.Json;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateTodo {
     String description;
     String note;
     
     @JsonFormat(pattern = "yyyy-MM-dd:HH:mm:ss")
     LocalDateTime finalDate;
}

