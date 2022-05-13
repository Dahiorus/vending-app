package me.dahiorus.project.vending.core.service;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.model.dto.VendingMachineDTO;

public interface CommentDtoService
{
  /**
   * Get the comments of a vending machine
   *
   * @param id The ID of the {@link VendingMachineDTO vending machine}
   * @return The {@link CommentDTO comments} of the vending machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  List<CommentDTO> getComments(UUID id) throws EntityNotFound;

  /**
   * Add a comment to a vending machine
   *
   * @param id      The ID of the {@link VendingMachineDTO vending machine}
   * @param comment The {@link CommentDTO comment} to add
   * @return The new comment
   * @throws EntityNotFound      if no vending machine match the ID
   * @throws ValidationException if the comment contains invalid data
   */
  CommentDTO comment(UUID id, CommentDTO comment) throws EntityNotFound, ValidationException;
}
