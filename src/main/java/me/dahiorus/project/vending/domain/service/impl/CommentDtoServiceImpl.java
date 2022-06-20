package me.dahiorus.project.vending.domain.service.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Comment;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.CommentDTO;
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
  private final DAO<Comment> dao;

  private final DAO<VendingMachine> vendingMachineDao;

  private final DtoValidator<CommentDTO> commentDtoValidator;

  private final DtoMapper dtoMapper;

  @Transactional(readOnly = true)
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

  @Transactional
  @Override
  public CommentDTO comment(final UUID id, final CommentDTO comment) throws EntityNotFound, ValidationException
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

    CommentDTO commentDto = dtoMapper.toDto(addedComment, CommentDTO.class);
    commentDto.setVendingMachineId(id);

    return commentDto;
  }
}
