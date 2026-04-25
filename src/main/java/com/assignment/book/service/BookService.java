package com.assignment.book.service;

import com.assignment.book.domain.Author;
import com.assignment.book.domain.Book;
import com.assignment.book.dto.AuthorRequest;
import com.assignment.book.dto.BookRequest;
import com.assignment.book.dto.BookUpdateRequest;
import com.assignment.book.repository.AuthorRepository;
import com.assignment.book.repository.BookRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;

    public BookService(BookRepository bookRepository, AuthorRepository authorRepository) {
        this.bookRepository = bookRepository;
        this.authorRepository = authorRepository;
    }

    @Transactional
    public Book addBook(BookRequest bookRequest) {
        Book book = toBook(bookRequest);

        if (bookRepository.existsById(book.getIsbn())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Book already exists");
        }

        book.setPrice(normalizePrice(book.getPrice()));
        book.setAuthors(checkAuthors(bookRequest.getAuthors()));
        return bookRepository.save(book);
    }

    @Transactional
    public Book updateBook(String isbn, BookUpdateRequest bookRequest) {
        Book existingBook = bookRepository.findById(isbn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));

        existingBook.setTitle(bookRequest.getTitle());
        existingBook.setPrice(normalizePrice(bookRequest.getPrice()));
        existingBook.setAuthors(checkAuthorsForUpdate(bookRequest.getAuthors()));
        existingBook.setYear(bookRequest.getYear());
        existingBook.setGenre(bookRequest.getGenre());

        return bookRepository.save(existingBook);
    }

    @Transactional(readOnly = true)
    public List<Book> findBooks(String title, List<String> authorNames) {
        if ((title == null || title.isBlank()) && (authorNames == null || authorNames.isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please provide either book title, author names or both.");
        }

        String normalizedTitle = normalize(title);
        List<String> normalizedAuthorNames = normalizeAuthorNames(authorNames);
        long authorCount = normalizedAuthorNames == null ? 0 : normalizedAuthorNames.size();

        return bookRepository.searchBooks(normalizedTitle, normalizedAuthorNames, authorCount);
    }

    @Transactional
    public void deleteBook(String isbn) {
        if (!bookRepository.existsById(isbn)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        }

        bookRepository.deleteById(isbn);
    }

    private List<Author> checkAuthors(List<AuthorRequest> authors) {
        if (authors == null || authors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one author is required");
        }

        List<Author> listOfAuthors = new ArrayList<>();

        for (AuthorRequest author : authors) {
            if (author == null || author.getName() == null || author.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each author must have a name");
            }

            listOfAuthors.add(resolveAuthor(author));
        }

        return listOfAuthors;
    }

    private List<Author> checkAuthorsForUpdate(List<AuthorRequest> authors) {
        if (authors == null || authors.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one author is required");
        }

        List<Author> listOfAuthors = new ArrayList<>();

        for (AuthorRequest author : authors) {
            if (author == null || author.getName() == null || author.getName().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Each author must have a name");
            }

            listOfAuthors.add(resolveAuthor(author));
        }

        return listOfAuthors;
    }

    private Author resolveAuthor(AuthorRequest authorRequest) {
        Author existingAuthor = authorRepository.findByName(authorRequest.getName()).orElse(null);
        if (existingAuthor != null) {
            return existingAuthor;
        }

        Author newAuthor = new Author();
        newAuthor.setName(authorRequest.getName());
        newAuthor.setBirthday(parseBirthdayIfPresent(authorRequest.getBirthday()));
        return authorRepository.save(newAuthor);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        return value.isBlank() ? null : value;
    }

    private List<String> normalizeAuthorNames(List<String> authorNames) {
        if (authorNames == null || authorNames.isEmpty()) {
            return null;
        }

        Set<String> normalizedAuthorNames = new LinkedHashSet<>();

        for (String authorName : authorNames) {
            if (authorName != null && !authorName.isBlank()) {
                normalizedAuthorNames.add(authorName);
            }
        }

        return normalizedAuthorNames.isEmpty() ? null : new ArrayList<>(normalizedAuthorNames);
    }

    private BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price is required");
        }

        if (price.scale() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Price must have up to 2 decimal places");
        }

        return price.setScale(2, RoundingMode.UNNECESSARY);
    }

    private LocalDate parseBirthdayIfPresent(String birthday) {
        if (birthday == null || birthday.isBlank()) {
            return null;
        }

        try {
            LocalDate date = LocalDate.parse(birthday, DateTimeFormatter.ofPattern("dd-MM-yyyy"));

            if (date.isAfter(LocalDate.now())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birthday cannot be in the future");
            }
            return date;
        } catch (DateTimeParseException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Birthday must be in dd-MM-yyyy format");
        }
    }

    private Book toBook(BookRequest request) {
        Book book = new Book();
        book.setIsbn(request.getIsbn());
        book.setTitle(request.getTitle());
        book.setYear(request.getYear());
        book.setPrice(request.getPrice());
        book.setGenre(request.getGenre());
        return book;
    }
}
