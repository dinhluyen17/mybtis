package com.example.batis2.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<?> externalApiHandler(ExternalApiException ex, WebRequest request) {
        ErrorMessage errorDetails = new ErrorMessage(400, ex.getMessage(), request.getDescription(false));
        System.out.println("test");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<?> globleExcpetionHandler(HttpClientErrorException ex, WebRequest request) {
        ErrorMessage errorDetails = new ErrorMessage(ex.getRawStatusCode(), ex.getMessage(), request.getDescription(false));
        System.out.println("exxx");
        return new ResponseEntity<>(errorDetails, HttpStatus.NOT_FOUND);
    }

}
