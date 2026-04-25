package com.assignment.book.repository;

import com.assignment.book.domain.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, String> {

    @Query("""
            select b
            from Book b
            join b.authors a
            where (:title is null or b.title = :title)
              and (:authorNames is null or a.name in :authorNames)
            group by b
            having :authorNames is null
               or count(distinct a.name) = :authorCount
            """)
    List<Book> searchBooks(@Param("title") String title,
                           @Param("authorNames") List<String> authorNames,
                           @Param("authorCount") long authorCount);
}
