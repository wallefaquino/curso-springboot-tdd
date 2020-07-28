package com.walleftech.ctdd.repositories;

import com.walleftech.ctdd.entities.Book;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("Teste")
@DataJpaTest
public class BookRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BookRepository repository;

    @Test
    @DisplayName("Deve retornar true quando o isbn informado já existir")
    public void returnTrueWhenIsbnExists() {

        String isbn = "123123";
        Book book = Book.builder().title("Fábrica de valore").author("Evandro Guedes").isbn(isbn).build();

        entityManager.persist(book);

        Boolean exist = repository.existsByIsbn(isbn);

        Assertions.assertThat(exist).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando o isbn informado não existir")
    public void returnFalseWhenIsbnExists() {

        String isbn = "123123";
        Book book = Book.builder().title("Fábrica de valore").author("Evandro Guedes").isbn(isbn).build();

        entityManager.persist(book);

        Boolean exist = repository.existsByIsbn(isbn);

        Assertions.assertThat(exist).isTrue();
    }
}
