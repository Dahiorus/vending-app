package me.dahiorus.project.vending.infrastructure.rest.exception;

public class InvalidTokenCreation extends RuntimeException {
  public InvalidTokenCreation(String message, Throwable cause) {
    super(message, cause);
  }
}
