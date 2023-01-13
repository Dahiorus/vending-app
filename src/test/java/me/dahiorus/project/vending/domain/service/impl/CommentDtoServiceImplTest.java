package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.util.TestUtils.successResults;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.CommentDAO;
import me.dahiorus.project.vending.domain.dao.VendingMachineDAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.Comment;
import me.dahiorus.project.vending.domain.model.ItemType;
import me.dahiorus.project.vending.domain.model.VendingMachine;
import me.dahiorus.project.vending.domain.model.dto.CommentDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;
import me.dahiorus.project.vending.domain.service.validation.impl.CommentDtoValidator;
import me.dahiorus.project.vending.util.VendingMachineBuilder;

@ExtendWith(MockitoExtension.class)
class CommentDtoServiceImplTest
{
  @Mock
  CommentDAO dao;

  @Mock
  VendingMachineDAO vendingMachineDao;

  @Mock
  CommentDtoValidator commentDtoValidator;

  CommentDtoServiceImpl dtoService;

  @BeforeEach
  void setUp()
  {
    dtoService = new CommentDtoServiceImpl(dao, vendingMachineDao, commentDtoValidator, new DtoMapperImpl());
  }

  @Nested
  class CommentTests
  {
    @Test
    void commentMachine() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);
      when(vendingMachineDao.save(machine)).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setRate(5);
      comment.setContent("This is a comment");
      when(commentDtoValidator.validate(comment)).thenReturn(successResults());
      when(dao.save(any())).then(returnsFirstArg());

      assertThatNoException().isThrownBy(() -> dtoService.comment(machine.getId(), comment));
      assertThat(machine.getComments()).isNotEmpty();
      verify(dao).save(any());
    }

    @Test
    void commentHasInvalidValue() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      CommentDTO comment = new CommentDTO();
      comment.setContent("This is a comment");

      when(commentDtoValidator.validate(comment)).then(invoc -> {
        ValidationResults results = new ValidationResults();
        results.addError(fieldError("rate", "validation.constraints.comment.rate_interval",
          "Error from test"));
        return results;
      });

      assertThatExceptionOfType(ValidationException.class)
        .isThrownBy(() -> dtoService.comment(machine.getId(), comment));
      verify(vendingMachineDao, never()).save(machine);
      verify(dao, never()).save(any());
    }

    @Test
    void commentUnknownMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class)
        .isThrownBy(() -> dtoService.comment(id, new CommentDTO()));
      verify(vendingMachineDao, never()).save(any());
      verify(dao, never()).save(any());
    }
  }

  @Nested
  class GetCommentsTests
  {
    @Test
    void getComments() throws Exception
    {
      VendingMachine machine = buildMachine(UUID.randomUUID(), ItemType.FOOD);
      Comment comment = new Comment();
      comment.setContent("This is a comment");
      comment.setRate(3);
      machine.addComment(comment);

      when(vendingMachineDao.read(machine.getId())).thenReturn(machine);

      List<CommentDTO> comments = dtoService.getComments(machine.getId());

      assertThat(comments).hasSize(1)
        .anySatisfy(c -> assertThat(c).hasFieldOrPropertyWithValue("content", comment.getContent())
          .hasFieldOrPropertyWithValue("rate", comment.getRate()));
    }

    @Test
    void getCommentsOfNonExistingMachine() throws Exception
    {
      UUID id = UUID.randomUUID();
      when(vendingMachineDao.read(id)).thenThrow(new EntityNotFound(VendingMachine.class, id));

      assertThatExceptionOfType(EntityNotFound.class).isThrownBy(() -> dtoService.getComments(id));
    }
  }

  static VendingMachine buildMachine(final UUID id, final ItemType itemType)
  {
    return VendingMachineBuilder.builder()
      .id(id)
      .itemType(itemType)
      .build();
  }
}
