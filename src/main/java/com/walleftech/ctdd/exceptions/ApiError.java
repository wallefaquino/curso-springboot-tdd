package com.walleftech.ctdd.exceptions;

import lombok.Getter;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class ApiError {

    private String httpStatus;
    private String httpMessage;
    private List<String> errors;

    public ApiError(String httpStatus, String httpMessage, BindingResult bindingResult) {

        this.httpStatus = httpStatus;
        this.httpMessage = httpMessage;
        this.errors = new ArrayList<>();
        bindingResult.getAllErrors().forEach(error -> this.errors.add(error.getDefaultMessage()));
    }

    public ApiError(String httpStatus, String httpMessage, RuntimeException ex) {
        this.httpStatus = httpStatus;
        this.httpMessage = httpMessage;
        this.errors = Arrays.asList(ex.getMessage());
    }
}
