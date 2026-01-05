package com.todolist.todolist.Json.Todo;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateTodoRequest {
     String description;

     @NotBlank(message = "La note est obligatoire")
     String note;
     
     @NotNull(message = "La date finale est obligatoire")
     @JsonFormat(pattern = "yyyy-MM-dd:HH:mm:ss")
     LocalDateTime finalDate;
}
