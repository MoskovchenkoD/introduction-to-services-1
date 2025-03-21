package com.epam.song.controller.advice;

import com.epam.song.dto.ErrorResponse;
import com.epam.song.exception.ErrorCodeException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class SongsControllerAdvice {

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleValidationException(ConstraintViolationException e) {
    Map<String, String> details = new HashMap<>();
    for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
      details.put(violation.getPropertyPath().toString(), violation.getMessage());
    }
    ErrorResponse errorResponse = new ErrorResponse("Validation error", details, String.valueOf(HttpStatus.BAD_REQUEST.value()));
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
    Map<String, String> details = new HashMap<>();
    e.getBindingResult().getFieldErrors().forEach(error ->
        details.put(error.getField(), error.getDefaultMessage())
    );
    ErrorResponse errorResponse = new ErrorResponse("Validation error", details, String.valueOf(HttpStatus.BAD_REQUEST.value()));
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
    ErrorResponse errorResponse = new ErrorResponse(
        String.format("Parameter '%s' is invalid", ex.getParameter().getParameter()), 
        String.valueOf(HttpStatus.BAD_REQUEST.value())
    );
    return ResponseEntity.badRequest().body(errorResponse);
  }

  @ExceptionHandler(ErrorCodeException.class)
  public ResponseEntity<ErrorResponse> handleErrorCodeException(ErrorCodeException e) {
    ErrorResponse errorResponse = new ErrorResponse(e.getMessage(), String.valueOf(e.getErrorCode()));
    return ResponseEntity.status(HttpStatus.valueOf(e.getErrorCode())).body(errorResponse);
  }

}
