package com.miguelrodriguez19.safecube.shared.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class WebExceptionHandler {

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidation(
      final MethodArgumentNotValidException ex) {

    final var errors =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(
                Collectors.toMap(FieldError::getField, this::getDefaultErrorMessage, (a, b) -> a));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, String>> handleConstraintViolation(
      final ConstraintViolationException ex) {

    final var errors =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (a, b) -> a));

    return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
  }

  private String getDefaultErrorMessage(final FieldError fieldError) {
    if (fieldError.getDefaultMessage() == null) {
      return "Malformed request";
    }
    return fieldError.getDefaultMessage();
  }
}
