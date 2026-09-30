package com.solicita.exception;

import java.time.Instant;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
        @ExceptionHandler(ResourceNotFoundException.class)
        public ResponseEntity<ErrorResponse> handleNotFound(
                        ResourceNotFoundException exception) {

                ErrorResponse response = new ErrorResponse(
                                exception.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.NOT_FOUND)
                                .body(response);
        }

        @ExceptionHandler(BusinessException.class)
        public ResponseEntity<ErrorResponse> handleBusiness(
                        BusinessException exception) {

                ErrorResponse response = new ErrorResponse(
                                exception.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.CONFLICT)
                                .body(response);
        }

        @ExceptionHandler(ToManyResourceRequisitionException.class)
        public ResponseEntity<ErrorResponse> handleManyResources(
                        ToManyResourceRequisitionException exception) {

                ErrorResponse response = new ErrorResponse(
                                exception.getMessage(),
                                Instant.now());

                return ResponseEntity
                                .status(HttpStatus.BAD_REQUEST)
                                .body(response);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ErrorResponse> handleValidation(
                        MethodArgumentNotValidException ex) {

                String message = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .findFirst()
                                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                                .orElse("Dados inválidos");

                return ResponseEntity
                                .badRequest()
                                .body(new ErrorResponse(message, Instant.now()));
        }

}
