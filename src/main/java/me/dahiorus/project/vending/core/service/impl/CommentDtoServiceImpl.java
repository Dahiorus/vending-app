package me.dahiorus.project.vending.core.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.AllArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.Comment;
import me.dahiorus.project.vending.core.model.VendingMachine;
import me.dahiorus.project.vending.core.model.dto.CommentDTO;
import me.dahiorus.project.vending.core.service.CommentDtoService;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.validation.CrudOperation;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Log4j2
@AllArgsConstructor
@Service
public class CommentDtoServiceImpl implements CommentDtoService
{
  private final DAO<Comment> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  private final DtoValidator<CommentDTO> commentDtoValidator;

  private final DtoMapper dtoMapper;

  @Transactional(readOnly = true, rollbackFor = EntityNotFound.class)
  @Override
  public List<CommentDTO> getComments(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = vendingMachineDao.read(id);
    List<CommentDTO> comments = entity.getComments()
      .stream()
      .map(comment -> dtoMapper.toDto(comment, CommentDTO.class))
      .toList();
    comments.forEach(comment -> comment.setVendingMachineId(id));

    return comments;
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void comment(final UUID id, final CommentDTO comment) throws EntityNotFound, ValidationException
  {
    log.traceEntry(() -> id, () -> comment);

    VendingMachine machine = vendingMachineDao.read(id);

    ValidationResults validationResults = commentDtoValidator.validate(comment);
    validationResults.throwIfError(comment, CrudOperation.CREATE);

    Comment commentToAdd = dtoMapper.toEntity(comment, Comment.class);
    machine.addComment(commentToAdd);
    dao.save(commentToAdd);
    vendingMachineDao.save(machine);

    log.info("Comment {} added to vending machine {}", comment, machine.getId());
  }
}
