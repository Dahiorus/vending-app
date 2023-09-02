package me.dahiorus.project.vending.domain.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Comment;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.CommentDto;
import me.dahiorus.project.vending.domain.service.CommentDtoService;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.validation.CrudOperation;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@RequiredArgsConstructor
@Service
public class CommentDtoServiceImpl implements CommentDtoService
{
  private final Dao<Comment> dao;

  private final Dao<VendingMachine> vendingMachineDao;

  private final DtoValidator<CommentDto> commentDtoValidator;

  private final DtoMapper dtoMapper;

  @Transactional(readOnly = true)
  @Override
  public List<CommentDto> getComments(final UUID id) throws EntityNotFound
  {
    VendingMachine entity = vendingMachineDao.read(id);
    List<CommentDto> comments = entity.getComments()
      .stream()
      .map(comment -> dtoMapper.toDto(comment, CommentDto.class))
      .toList();
    comments.forEach(comment -> comment.setVendingMachineId(id));

    return comments;
  }

  @Transactional
  @Override
  public CommentDto comment(final UUID id, final CommentDto comment) throws EntityNotFound, ValidationException
  {
    log.traceEntry(() -> id, () -> comment);

    VendingMachine machine = vendingMachineDao.read(id);

    ValidationResults validationResults = commentDtoValidator.validate(comment);
    validationResults.throwIfError(comment, CrudOperation.CREATE);

    Comment commentToAdd = dtoMapper.toEntity(comment, Comment.class);
    machine.addComment(commentToAdd);
    Comment addedComment = dao.save(commentToAdd);
    vendingMachineDao.save(machine);

    log.info("Comment {} added to vending machine {}", addedComment, machine.getId());

    CommentDto commentDto = dtoMapper.toDto(addedComment, CommentDto.class);
    commentDto.setVendingMachineId(id);

    return commentDto;
  }
}
