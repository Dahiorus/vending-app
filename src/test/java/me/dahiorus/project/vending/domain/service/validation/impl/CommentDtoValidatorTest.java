package me.dahiorus.project.vending.domain.service.validation.impl;

import static me.dahiorus.project.vending.util.ValidationTestUtils.assertHasExactlyFieldErrors;
import static org.assertj.core.api.Assertions.assertThat;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import me.dahiorus.project.vending.domain.dao.CommentDAO;
import me.dahiorus.project.vending.domain.model.dto.CommentDTO;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@ExtendWith(MockitoExtension.class)
class CommentDtoValidatorTest
{
  @Mock
  CommentDAO dao;

  CommentDtoValidator validator;

  CommentDTO dto;

  @BeforeEach
  void setUp() throws Exception
  {
    validator = new CommentDtoValidator(dao);
  }

  CommentDTO buildDto(final Integer rate, final String content)
  {
    CommentDTO dto = new CommentDTO();
    dto.setRate(rate);
    dto.setContent(content);

    return dto;
  }

  @Test
  void dtoIsValid()
  {
    dto = buildDto(5, "This is a comment");

    ValidationResults results = validator.validate(dto);

    assertThat(results.hasError()).as("No error on %s", dto)
      .isFalse();
  }

  @ParameterizedTest(name = "Rate value [{0}] is not valid")
  @NullSource
  @ValueSource(ints = { -1, 6 })
  void rateMustBeBetween0And5(final Integer rate)
  {
    dto = buildDto(rate, "This is a comment");

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "rate", "validation.constraints.comment.rate_interval");
  }

  @Test
  void contentHasMaxLength()
  {
    dto = buildDto(4, RandomStringUtils.randomAlphanumeric(1025));

    ValidationResults results = validator.validate(dto);

    assertHasExactlyFieldErrors(results, "content", "validation.constraints.max_length");
  }
}
