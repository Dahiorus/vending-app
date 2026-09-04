package me.dahiorus.project.vending.infrastructure.rest.exception;

public class UserNotAuthenticated extends RuntimeException {
  public UserNotAuthenticated() {
    this("User not authenticated");
  }

  public UserNotAuthenticated(String message) {
    super(message);
  }
}
