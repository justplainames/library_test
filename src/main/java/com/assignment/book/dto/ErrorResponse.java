package com.assignment.book.dto;

public record ErrorResponse(
        boolean success,
        String message
) {
    public static ErrorResponse failure(String message) {
        return new ErrorResponse(false, message);
    }
}
