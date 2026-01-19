package com.miguelrodriguez19.safecube.shared.exception.model;

import java.util.Map;

public record ErrorResponse(String error, Map<String, String> fields) {
  public static ErrorResponse simple(final String error) {
    return new ErrorResponse(error, null);
  }

  public static ErrorResponse withFields(final String error, final Map<String, String> fields) {
    return new ErrorResponse(error, fields);
  }
}
