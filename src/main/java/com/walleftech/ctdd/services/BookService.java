package com.walleftech.ctdd.services;

import com.walleftech.ctdd.entities.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface BookService {

    Book save(Book book);

    Optional<Book> findById(Long id);

    void deleteById(Long id);

    Book updateById(Long id, Book book);

    Page findByFilter(Book book, Pageable pageRequest);
}
