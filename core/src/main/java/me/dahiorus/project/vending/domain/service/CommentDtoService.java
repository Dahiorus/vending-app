package me.dahiorus.project.vending.domain.service;

import java.util.List;
import java.util.UUID;

import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.dto.CommentDto;
import me.dahiorus.project.vending.domain.model.dto.VendingMachineDto;

public interface CommentDtoService
{
  /**
   * Get the comments of a vending machine
   *
   * @param id The ID of the {@link VendingMachineDto vending machine}
   * @return The {@link CommentDto comments} of the vending machine
   * @throws EntityNotFound if no vending machine match the ID
   */
  List<CommentDto> getComments(UUID id) throws EntityNotFound;

  /**
   * Add a comment to a vending machine
   *
   * @param id      The ID of the {@link VendingMachineDto vending machine}
   * @param comment The {@link CommentDto comment} to add
   * @return The new comment
   * @throws EntityNotFound      if no vending machine match the ID
   * @throws ValidationException if the comment contains invalid data
   */
  CommentDto comment(UUID id, CommentDto comment) throws EntityNotFound, ValidationException;
}
