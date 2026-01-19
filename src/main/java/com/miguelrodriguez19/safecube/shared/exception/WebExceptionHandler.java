package com.miguelrodriguez19.safecube.shared.exception;

import com.miguelrodriguez19.safecube.shared.exception.model.ErrorResponse;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.util.HashMap;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

/**
 * WebExceptionHandler
 *
 * <p>Centralized HTTP exception handling for the application.
 */
@Slf4j
@RestControllerAdvice
public class WebExceptionHandler {

  public static final String VALIDATION_FAILED = "VALIDATION_FAILED";

  // Validation errors (body / params)

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
      final MethodArgumentNotValidException ex) {

    log.error("Request body validation failed", ex);

    final var fields =
        ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField, this::defaultMessage, (a, b) -> a));

    return ResponseEntity.badRequest().body(ErrorResponse.withFields(VALIDATION_FAILED, fields));
  }

  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolation(
      final ConstraintViolationException ex) {

    log.error("Constraint violation", ex);

    final var fields =
        ex.getConstraintViolations().stream()
            .collect(
                Collectors.toMap(
                    v -> v.getPropertyPath().toString(),
                    ConstraintViolation::getMessage,
                    (a, b) -> a));

    return ResponseEntity.badRequest().body(ErrorResponse.withFields(VALIDATION_FAILED, fields));
  }

  @ExceptionHandler(HandlerMethodValidationException.class)
  public ResponseEntity<ErrorResponse> handleHandlerMethodValidation(
      final HandlerMethodValidationException ex) {

    log.error("Handler method validation failed", ex);

    final var fields = new HashMap<String, String>();

    ex.getAllErrors()
        .forEach(
            error -> {
              final var field = resolveFieldName(error);
              fields.put(field, defaultMessage(error));
            });

    return ResponseEntity.badRequest().body(ErrorResponse.withFields(VALIDATION_FAILED, fields));
  }

  // Domain & infrastructure errors

  @ExceptionHandler(DomainException.class)
  public ResponseEntity<ErrorResponse> handleDomainException(final DomainException ex) {

    log.error("Domain exception", ex);

    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(ErrorResponse.simple(ex.getMessage()));
  }

  @ExceptionHandler(InfrastructureException.class)
  public ResponseEntity<ErrorResponse> handleInfrastructureException(
      final InfrastructureException ex) {

    log.error("Infrastructure exception", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.simple("Internal server error"));
  }

  // Fallback

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(final Exception ex) {

    log.error("Unexpected exception", ex);

    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ErrorResponse.simple("Unexpected error"));
  }

  // Helpers

  private String resolveFieldName(final MessageSourceResolvable error) {
    if (error instanceof FieldError fieldError) {
      return fieldError.getField();
    }

    final var codes = error.getCodes();
    if (codes != null && codes.length > 0) {
      final var code = codes[0];
      final var lastDot = code.lastIndexOf('.');
      if (lastDot != -1 && lastDot < code.length() - 1) {
        return code.substring(lastDot + 1);
      }
    }

    return "unknown";
  }

  private String defaultMessage(final MessageSourceResolvable error) {
    return error.getDefaultMessage() != null ? error.getDefaultMessage() : "Malformed request";
  }
}
