package me.dahiorus.project.vending.domain.exception;

public class InvalidRefreshToken extends RuntimeException {
  public InvalidRefreshToken(String message) {
    super(message);
  }
}
