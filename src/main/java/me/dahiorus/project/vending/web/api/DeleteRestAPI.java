package me.dahiorus.project.vending.web.api;

import java.util.UUID;

import org.springframework.http.ResponseEntity;

public interface DeleteRestAPI
{
  ResponseEntity<Void> delete(UUID id);
}
