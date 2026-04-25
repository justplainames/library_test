package com.assignment.book.controller;

import com.assignment.book.dto.ApiResponse;
import com.assignment.book.dto.BookRequest;
import com.assignment.book.dto.BookUpdateRequest;
import com.assignment.book.domain.Book;
import com.assignment.book.service.BookService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> findBooks(@RequestParam(required = false) String title,
                                                             @RequestParam(required = false) List<String> authorNames) {
        List<Book> books = bookService.findBooks(title, authorNames);

        if (books.isEmpty()) {
            return ResponseEntity.ok(ApiResponse.success("No books found", books));
        }

        return ResponseEntity.ok(ApiResponse.success("Books retrieved successfully", books));
    }


    @PostMapping
    public ResponseEntity<ApiResponse<Book>> addBook(@Valid @RequestBody BookRequest bookRequest) {
        Book savedBook = bookService.addBook(bookRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Book added successfully", savedBook));
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<ApiResponse<Book>> updateBook(@PathVariable String isbn, @Valid @RequestBody BookUpdateRequest bookRequest) {
        Book updatedBook = bookService.updateBook(isbn, bookRequest);
        return ResponseEntity.ok(ApiResponse.success("Book updated successfully", updatedBook));
    }


    @DeleteMapping("/{isbn}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable String isbn) {
        bookService.deleteBook(isbn);
        return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
    }
}
