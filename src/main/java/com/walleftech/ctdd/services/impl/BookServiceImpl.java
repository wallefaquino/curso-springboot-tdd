package com.walleftech.ctdd.services.impl;

import com.walleftech.ctdd.exceptions.BusinessException;
import com.walleftech.ctdd.exceptions.ResourceNotFoundException;
import com.walleftech.ctdd.repositories.BookRepository;
import com.walleftech.ctdd.entities.Book;
import com.walleftech.ctdd.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class BookServiceImpl implements BookService {

    BookRepository repository;

    public BookServiceImpl(BookRepository repository) {
        this.repository = repository;
    }

    @Override
    public Book save(Book book) {

        log.info("Salvando livro: " + book.getTitle());

        if(repository.existsByIsbn(book.getIsbn())) {
            throw new BusinessException("Isbn já registrado!");
        }
        return repository.save(book);
    }

    @Override
    public Optional<Book> findById(Long id) {

        log.info("Buscando livro de id: " + id);

        Book book = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource Not Found!"));

        return Optional.of(book);
    }

    @Override
    public void deleteById(Long id) {

        log.info("Deletando livro com id: " + id);

        Book book = findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resource Not Found"));

        repository.deleteById(book.getId());
    }

    @Override
    public Book updateById(Long id, Book bookUpdated) {

        log.info("Atualizando livro com id: " + id);

        Book book = findById(id)
                .map(bookUpdate -> repository.save(bookUpdated))
                .orElseThrow(() -> new ResourceNotFoundException("Resource Not Found"));

        return book;
    }

    @Override
    public Page findByFilter(Book filter, Pageable pageRequest) {

        log.info("Buscando livros!");

        Example example = Example.of(filter,
                ExampleMatcher
                        .matching()
                        .withIgnoreCase()
                        .withIgnoreNullValues()
                        .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
        );

        return repository.findAll(example, pageRequest);
    }

}
