package com.assignment.book.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BookUpdateRequest {

    @NotBlank
    private String title;

    @NotEmpty
    @Valid
    private List<AuthorRequest> authors;

    @NotNull
    private Integer year;

    @NotNull
    private BigDecimal price;

    @NotBlank
    private String genre;
}
