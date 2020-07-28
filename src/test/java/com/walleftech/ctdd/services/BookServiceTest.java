package com.walleftech.ctdd.services;

import com.walleftech.ctdd.exceptions.BusinessException;
import com.walleftech.ctdd.exceptions.ResourceNotFoundException;
import com.walleftech.ctdd.repositories.BookRepository;
import com.walleftech.ctdd.entities.Book;
import com.walleftech.ctdd.services.impl.BookServiceImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.PATH;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("Test")
public class BookServiceTest {

    private BookService service;

    @MockBean
    private BookRepository repository;

    @BeforeEach
    public void setUp() {
        this.service = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Deve salvar um livro com sucesso")
    public void saveBookTest() {

        Book book = Book.builder()
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        when(repository.existsByIsbn(book.getIsbn())).thenReturn(false);

        when(repository.save(book))
                .thenReturn(Book.builder()
                        .id(Long.valueOf(1))
                        .title("Fábrica de Valores")
                        .author("Evandro Guedes")
                        .isbn("123123")
                        .build());

        Book savedBook = service.save(book);

        assertThat(savedBook.getId()).isNotNull();
        assertThat(savedBook.getTitle()).isEqualTo("Fábrica de Valores");
        assertThat(savedBook.getAuthor()).isEqualTo("Evandro Guedes");
        assertThat(savedBook.getIsbn()).isEqualTo("123123");
    }

    @Test
    @DisplayName("Deve lançar uma exceção de negócio ao tentar salvar um livro com isbn já existente")
    public void shouldThrowBusinessExceptionTest() {

        Book book = Book.builder()
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        when( repository.existsByIsbn(Mockito.anyString()) ).thenReturn(true);

        Throwable exception = Assertions.catchThrowable(() -> service.save(book));

        assertThat(exception)
                .isInstanceOf(BusinessException.class)
                .hasMessage("Isbn já registrado!");

        Mockito.verify(repository, Mockito.never()).save(book);
    }

    @Test
    @DisplayName("Deve retornar um livro por id")
    public void findByIdTest() {
        Book book = Book.builder()
                .id(Long.valueOf(12))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        when(repository.findById(book.getId())).thenReturn(Optional.of(book));

        Optional<Book> bookReturned = repository.findById(book.getId());

        assertThat(bookReturned.isPresent()).isTrue();
        assertThat(bookReturned.get().getId()).isEqualTo(book.getId());
        assertThat(bookReturned.get().getAuthor()).isEqualTo(book.getAuthor());
        assertThat(bookReturned.get().getTitle()).isEqualTo(book.getTitle());
        assertThat(bookReturned.get().getIsbn()).isEqualTo(book.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar uma exception de recurso não encontrado quando o livro não existir")
    public void shouldThrowResourceNotFoundTest() {

        when(repository.findById(anyLong()))
                .thenThrow(new ResourceNotFoundException("Resource Not Found!"));

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> repository.findById(Long.valueOf(11)));

        assertThat(exception.getMessage()).isEqualTo("Resource Not Found!");
    }

    @Test
    @DisplayName("Deve deletar um livro por id")
    public void deleteByIdTest() {
        Book book = Book.builder()
                .id(Long.valueOf(12))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();
        when(repository.findById(anyLong())).thenReturn(Optional.of(book));

        assertDoesNotThrow(() -> service.deleteById(Long.valueOf(51)));

        Mockito.verify(repository, Mockito.times(1)).deleteById(anyLong());

    }

    @Test
    @DisplayName("Deve retornar uma exceção quando o livro não existir")
    public void sholdThrowResourceNotFoundExceptionTest() {

        when(repository.findById(anyLong())).thenThrow(new ResourceNotFoundException("Resource Not Found"));

        Throwable exception = Assertions.catchThrowable(() -> service.deleteById(Long.valueOf(88)));

        assertThat(exception).isInstanceOf(ResourceNotFoundException.class);
        assertThat(exception.getMessage()).isEqualTo("Resource Not Found");
    }

    @Test
    @DisplayName("Deve atualizar um livro")
    public void updateBookTest() {
        Book book = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        Book newBook = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores Vol. 2")
                .author("Evandro Guedes")
                .isbn("123123").build();

        when(repository.findById(Long.valueOf(1))).thenReturn(Optional.of(book));
        when(repository.save(newBook)).thenReturn(newBook);

        Book bookUpdated = service.updateById(book.getId(), newBook);

        assertThat(bookUpdated.getId()).isEqualTo(newBook.getId());
        assertThat(bookUpdated.getTitle()).isEqualTo(newBook.getTitle());
        assertThat(bookUpdated.getAuthor()).isEqualTo(newBook.getAuthor());
        assertThat(bookUpdated.getIsbn()).isEqualTo(newBook.getIsbn());
    }

    @Test
    @DisplayName("Deve retornar uma exceção quando o livro não existir")
    public void shouldThrowResourceNotFoundExceptionTest() {

        when(repository.findById(anyLong())).thenThrow(new ResourceNotFoundException("Resource Not Found"));

        Throwable exception = Assertions.catchThrowable(() -> service.updateById(Long.valueOf(11), any()));

        assertThat(exception).isInstanceOf(ResourceNotFoundException.class);
        assertThat(exception.getMessage()).isEqualTo("Resource Not Found");
    }

    @Test
    @DisplayName("Deve buscar um livro com base nos filtros informados")
    public void findByFiltersTest() {

        Book book = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Book> lista = Arrays.asList(book);
        Page<Book> page = new PageImpl<Book>(lista, pageRequest, 1);

        when(repository.findAll(Mockito.any(Example.class), Mockito.any(PageRequest.class)))
                .thenReturn(page);

        Page<Book> result = service.findByFilter(book, pageRequest);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).isEqualTo(lista);
        assertThat(result.getPageable().getPageNumber()).isEqualTo(0);
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);
    }
}
