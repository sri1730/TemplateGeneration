package com.example.TemplateGeneration.Dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Data
public class ResponsDTO {
    private HttpStatus status;
    private String message;
}
