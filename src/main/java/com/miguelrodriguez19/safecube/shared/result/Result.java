package com.miguelrodriguez19.safecube.shared.result;

import java.util.Objects;
import java.util.Optional;

/**
 * Result
 *
 * <p>Represents the outcome of an application use case execution.
 *
 * <p>A {@code Result} is either a success holding a value of type {@code S}, or a failure holding
 * an error of type {@code E}.
 *
 * @param <S> success payload type
 * @param <E> error type
 */
public sealed interface Result<S, E> permits Result.Success, Result.Failure {

  /**
   * Indicates whether this result represents a successful execution.
   *
   * @return {@code true} if success, {@code false} otherwise
   */
  boolean isSuccess();

  /**
   * Indicates whether this result represents a failed execution.
   *
   * @return {@code true} if failure, {@code false} otherwise
   */
  default boolean isFailure() {
    return !isSuccess();
  }

  /**
   * Returns the success value if present.
   *
   * @return an optional success value
   */
  Optional<S> success();

  /**
   * Returns the error value if present.
   *
   * @return an optional error value
   */
  Optional<E> error();

  /**
   * Creates a successful result.
   *
   * @param value the success value
   * @param <S> success payload type
   * @param <E> error type
   * @return a success result
   */
  static <S, E> Result<S, E> success(final S value) {
    return new Success<>(value);
  }

  /**
   * Creates a failed result.
   *
   * @param error the error value
   * @param <S> success payload type
   * @param <E> error type
   * @return a failure result
   */
  static <S, E> Result<S, E> failure(final E error) {
    return new Failure<>(error);
  }

  /** Success result implementation. */
  final class Success<S, E> implements Result<S, E> {

    private final S value;

    private Success(final S value) {
      this.value = Objects.requireNonNull(value, "value must not be null");
    }

    @Override
    public boolean isSuccess() {
      return true;
    }

    @Override
    public Optional<S> success() {
      return Optional.of(value);
    }

    @Override
    public Optional<E> error() {
      return Optional.empty();
    }
  }

  /** Failure result implementation. */
  final class Failure<S, E> implements Result<S, E> {

    private final E error;

    private Failure(final E error) {
      this.error = Objects.requireNonNull(error, "error must not be null");
    }

    @Override
    public boolean isSuccess() {
      return false;
    }

    @Override
    public Optional<S> success() {
      return Optional.empty();
    }

    @Override
    public Optional<E> error() {
      return Optional.of(error);
    }
  }
}
