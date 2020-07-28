package com.walleftech.ctdd.repositories;

import com.walleftech.ctdd.entities.Book;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookRepository extends JpaRepository<Book, Long> {

    Boolean existsByIsbn(String isbn);
}
