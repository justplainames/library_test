package com.assignment.book.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthorRequest {

    @NotBlank
    private String name;

    private String birthday;
}
