package com.walleftech.ctdd.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.walleftech.ctdd.dto.BookDTO;
import com.walleftech.ctdd.entities.Book;
import com.walleftech.ctdd.exceptions.BusinessException;
import com.walleftech.ctdd.exceptions.ResourceNotFoundException;
import com.walleftech.ctdd.services.BookService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.BDDMockito;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.InstanceOfAssertFactories.PATH;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@ActiveProfiles("Test")
@WebMvcTest
@AutoConfigureMockMvc
public class BookAPITest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private BookService service;

    private static final String PATH = "/v1/api/books";

    @Test
    @DisplayName("Deve criar um livro")
    public void createBookTest() throws Exception {

        Book book = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        BookDTO bookDTO = BookDTO.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        BDDMockito.when(service.save(Mockito.any(Book.class))).thenReturn(book);

        String json = new ObjectMapper().writeValueAsString(bookDTO);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(jsonPath("id").isNotEmpty())
                .andExpect(jsonPath("title").value(bookDTO.getTitle()))
                .andExpect(jsonPath("author").value(bookDTO.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookDTO.getIsbn()));
    }

    @Test
    @DisplayName("Deve lançar um exceção quando não houver informações suficientes para criação")
    public void createBookWithValidationErrorTest() throws Exception {

        String json = new ObjectMapper().writeValueAsString(new BookDTO());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("httpStatus").value("400") )
                .andExpect( jsonPath("httpMessage").value("Bad Request") )
                .andExpect( jsonPath("errors", hasSize(3)));
    }

    @Test
    @DisplayName("Deve lançar uma exceção de negócio ao tentar salvar um livro com mesmo isbn")
    public void createBookWithBusinessExceptionTest() throws Exception {

        Book book = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        String json = new ObjectMapper().writeValueAsString(book);
        String mensagemErro = "Livro com esse isbn já existe!";

        BDDMockito.given(service.save(book)).willThrow(new BusinessException(mensagemErro));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .post(PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(json);

        mvc.perform(request)
                .andExpect( status().isBadRequest() )
                .andExpect( jsonPath("httpStatus").value("400") )
                .andExpect( jsonPath("httpMessage").value("Bad Request") )
                .andExpect( jsonPath("errors", hasSize(1)))
                .andExpect( jsonPath("errors[0]").value(mensagemErro));
    }

    @Test
    @DisplayName("Deve salvar um livro com sucesso")
    public void shouldFindBookTest() throws Exception {

        Long id = Long.valueOf(11);

        Book book = Book.builder()
                .id(id)
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        BDDMockito.given(service.findById(id)).willReturn(Optional.of(book));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(PATH.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect( status().isOk() )
                .andExpect( jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(book.getTitle()))
                .andExpect(jsonPath("author").value(book.getAuthor()))
                .andExpect(jsonPath("isbn").value(book.getIsbn()));

    }

    @Test
    @DisplayName("Deve lancar uma exceção de não encontrado quando buscar um livro que não existe")
    public void shouldThrowNotFoundExceptionTest() throws Exception {

        Long id = Long.valueOf(11);

        Book book = Book.builder()
                .id(id)
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        BDDMockito.given(service.findById(id)).willThrow(new ResourceNotFoundException("Resource Not Found"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(PATH.concat("/" + id))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("httpStatus").value("404"))
                .andExpect(jsonPath("httpMessage").value("Resource Not Found"));
    }

    @Test
    @DisplayName("Deve deletar um livro com sucesso")
    public void deleteBookTestTest() throws Exception {

        BDDMockito.given(service.findById(anyLong()))
                .willReturn(Optional.of(Book.builder().id(Long.valueOf(1)).build()));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(PATH.concat("/1"))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("Deve lançar uma exceção de recurso não encontrado quando tentar deletar um livro que não existe")
    public void shouldThrowRecourseNotFoundExceptionTest() throws Exception {

        BDDMockito.given(service.findById(anyLong()))
                .willThrow(new ResourceNotFoundException("Resource Not Found"));

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .delete(PATH.concat("/1"))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("httpStatus").value("404"))
                .andExpect(jsonPath("httpMessage").value("Resource Not Found"));
    }

    @Test
    @DisplayName("Deve atualizar um livro com sucesso")
    public void updateBookTest() throws Exception {

        Long id = Long.valueOf(1);

        Book bookUpdated = Book.builder()
                .id(id)
                .title("Fábrica de Valores Volume 1")
                .author("Evandro Guedes")
                .isbn("312")
                .build();

        String json = new ObjectMapper().writeValueAsString(bookUpdated);

        Book book = Book.builder()
                .id(id)
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123")
                .build();

        BDDMockito.given(service.findById(id)).willReturn(Optional.of(book));
        BDDMockito.given(service.updateById(id, bookUpdated)).willReturn(bookUpdated);

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .put(PATH.concat("/") + id)
                .content(json)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON);


        mvc.perform(request)
                .andExpect(status().isOk())
                .andExpect(jsonPath("id").value(id))
                .andExpect(jsonPath("title").value(bookUpdated.getTitle()))
                .andExpect(jsonPath("author").value(bookUpdated.getAuthor()))
                .andExpect(jsonPath("isbn").value(bookUpdated.getIsbn()));
    }

    @Test
    @DisplayName("Deve buscar livros com base nos filtros")
    public void findByFiltersTest() throws Exception {

        Long id = Long.valueOf(11);

        Book book = Book.builder()
                .id(Long.valueOf(1))
                .title("Fábrica de Valores")
                .author("Evandro Guedes")
                .isbn("123123").build();

        BDDMockito.given(service.findByFilter(Mockito.any(Book.class), Mockito.any(Pageable.class)))
                .willReturn(new PageImpl<Book>(Arrays.asList(book), PageRequest.of(0, 100), 1 ));


        String queryString = String.format("?title=%s&author=%s&page=0&size=100",
                book.getTitle(), book.getAuthor());

        MockHttpServletRequestBuilder request = MockMvcRequestBuilders
                .get(PATH.concat(queryString))
                .accept(MediaType.APPLICATION_JSON);

        mvc.perform(request)
                .andExpect( status().isOk())
                .andExpect( jsonPath("content", Matchers.hasSize(1)) )
                .andExpect( jsonPath("totalElements").value(1) )
                .andExpect( jsonPath("pageable.pageSize").value(100) )
                .andExpect( jsonPath("pageable.pageNumber").value(0) );
    }
}
