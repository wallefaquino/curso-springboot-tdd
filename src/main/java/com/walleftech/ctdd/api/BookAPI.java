package com.walleftech.ctdd.api;

import com.walleftech.ctdd.dto.BookDTO;
import com.walleftech.ctdd.entities.Book;
import com.walleftech.ctdd.exceptions.ApiError;
import com.walleftech.ctdd.exceptions.BusinessException;
import com.walleftech.ctdd.exceptions.ResourceNotFoundException;
import com.walleftech.ctdd.services.BookService;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/api/books")
@Slf4j
public class BookAPI {

    private BookService service;
    private ModelMapper mapper;

    public BookAPI(BookService service, ModelMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    public ResponseEntity<BookDTO> create( @Valid @RequestBody BookDTO dto) {

        log.info("Livro: " + dto.getTitle() + " recebido");

        Book book = mapper.map(dto, Book.class);
        book = service.save(book);

        log.info("Livro salvo com sucesso!");

        BookDTO bookDTO = mapper.map(book, BookDTO.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(bookDTO);

    }

    @GetMapping("/{id}")
    public ResponseEntity<BookDTO> findById(@PathVariable Long id) {
        Book book = service.findById(id).get();

        log.info("Livro: " + book.getTitle() + " encontrado");
        BookDTO dto = mapper.map(book, BookDTO.class);

        return ResponseEntity.ok().body(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookDTO> updateById(@PathVariable Long id, @Valid @RequestBody BookDTO dto) {
        Book book = service.findById(id).get();

        Book updatedBook = mapper.map(dto, Book.class);

        updatedBook = service.updateById(book.getId(), updatedBook);

        log.info("Livro atualizado!");

        BookDTO bookDTO = mapper.map(updatedBook, BookDTO.class);

        return ResponseEntity.ok(bookDTO);


    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        Book book  = service.findById(id).get();
        service.deleteById(book.getId());

        log.info("Livro deletado!");
    }

    @GetMapping
    public Page<BookDTO> findByFilters(BookDTO dto, Pageable pageRequest) {
        Book filters = mapper.map(dto, Book.class);
        Page<Book> result = service.findByFilter(filters, pageRequest);

        List<BookDTO> list = result.getContent().stream()
                .map(book -> mapper.map(book, BookDTO.class))
                .collect(Collectors.toList());

        log.info("Livros encontrados!");

        return new PageImpl<BookDTO>(list, pageRequest, result.getTotalElements());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleValidationException(MethodArgumentNotValidException ex) {

        String httpStatus = "400";
        String httpMessage = "Bad Request";
        BindingResult bindingResult = ex.getBindingResult();

        log.error(ex.getMessage());

        return new ApiError(httpStatus,httpMessage, bindingResult);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiError handleBusinessException(BusinessException ex) {

        String httpStatus = "400";
        String httpMessage = "Bad Request";

        log.error(ex.getMessage());

        return new ApiError(httpStatus, httpMessage, ex);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleResourceNotFoundException(ResourceNotFoundException ex) {

        String httpStatus = "404";
        String httpMessage = "Resource Not Found";

        log.error(ex.getMessage());

        return new ApiError(httpStatus, httpMessage, ex);
    }
}
