package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.User_;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.model.dto.UserWithPasswordDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.core.service.validation.CrudOperation;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;
import me.dahiorus.project.vending.core.service.validation.PasswordValidator;
import me.dahiorus.project.vending.core.service.validation.ValidationResults;

@Log4j2
@Service
public class UserDtoServiceImpl extends DtoServiceImpl<User, UserDTO, UserDAO> implements UserDtoService
{
  private final PasswordValidator passwordValidator;

  private final PasswordEncoder passwordEncoder;

  public UserDtoServiceImpl(final UserDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<UserDTO> dtoValidator, final PasswordValidator passwordValidator,
      final PasswordEncoder passwordEncoder)
  {
    super(dao, dtoMapper, dtoValidator);
    this.passwordValidator = passwordValidator;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  public Logger getLogger()
  {
    return log;
  }

  @Override
  protected Class<UserDTO> getDomainClass()
  {
    return UserDTO.class;
  }

  @Transactional(rollbackFor = ValidationException.class)
  @Override
  public UserDTO create(final UserWithPasswordDTO dto) throws ValidationException
  {
    getLogger().debug("Creating a new {} with a password: {}", getDomainClass().getSimpleName(), dto);

    // validate the password hard coded policy and encode it
    validate(dto, CrudOperation.CREATE);
    dto.setPassword(passwordEncoder.encode(dto.getPassword()));

    User createdEntity = dao.save(dtoMapper.toEntity(dto, entityClass));
    UserDTO createdDto = dtoMapper.toDto(createdEntity, getDomainClass());

    getLogger().info("{} created: {}", getDomainClass().getSimpleName(), createdDto);

    return createdDto;
  }

  @Override
  protected void doExtraValidation(final UserDTO dto, final ValidationResults validationResults)
  {
    if (dto instanceof UserWithPasswordDTO userWithPwd)
    {
      log.debug("Validating the user's password: {}", dto);
      ValidationResults pwdValidationResults = passwordValidator.validate(User_.PASSWORD, userWithPwd.getPassword(),
          true);
      validationResults.mergeFieldErrors(pwdValidationResults);
    }
  }
}
