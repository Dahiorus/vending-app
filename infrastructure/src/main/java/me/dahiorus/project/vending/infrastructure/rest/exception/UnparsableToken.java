package me.dahiorus.project.vending.infrastructure.rest.exception;

public class UnparsableToken extends RuntimeException {
  public UnparsableToken(String message, Throwable cause) {
    super(message, cause);
  }
}
