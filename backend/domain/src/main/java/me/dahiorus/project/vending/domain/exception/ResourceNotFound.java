package me.dahiorus.project.vending.domain.exception;

import me.dahiorus.project.vending.domain.DomainId;

public class ResourceNotFound extends RuntimeException {
  public ResourceNotFound(String message) {
    super(message);
  }

  public ResourceNotFound(DomainId id) {
    this("Resource not found with ID: " + id);
  }
}
