package me.dahiorus.project.vending.core.service.impl;

import static me.dahiorus.project.vending.core.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.core.service.validation.ValidationError.getFullCode;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.DAO;
import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.exception.EntityNotFound;
import me.dahiorus.project.vending.core.exception.ValidationException;
import me.dahiorus.project.vending.core.model.AppUser;
import me.dahiorus.project.vending.core.model.AppUser_;
import me.dahiorus.project.vending.core.model.BinaryData;
import me.dahiorus.project.vending.core.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.core.model.dto.EditPasswordDTO;
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
public class UserDtoServiceImpl extends DtoServiceImpl<AppUser, UserDTO, UserDAO> implements UserDtoService
{
  private final PasswordValidator passwordValidator;

  private final PasswordEncoder passwordEncoder;

  private final DAO<BinaryData> binaryDataDao;

  public UserDtoServiceImpl(final UserDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<UserDTO> dtoValidator, final PasswordValidator passwordValidator,
      final PasswordEncoder passwordEncoder, final DAO<BinaryData> binaryDataDao)
  {
    super(dao, dtoMapper, dtoValidator);
    this.passwordValidator = passwordValidator;
    this.passwordEncoder = passwordEncoder;
    this.binaryDataDao = binaryDataDao;
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

    AppUser entity = dtoMapper.toEntity(dto, entityClass);
    entity.setPassword(passwordEncoder.encode(dto.getPassword()));

    AppUser createdEntity = dao.save(entity);
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
      ValidationResults pwdValidationResults = passwordValidator.validate(AppUser_.PASSWORD, userWithPwd.getPassword(),
          true);
      validationResults.mergeFieldErrors(pwdValidationResults);
    }
  }

  @Transactional(readOnly = true)
  @Override
  public UserDTO getByUsername(final String username) throws EntityNotFound
  {
    return dao.findByEmail(username)
      .map(user -> dtoMapper.toDto(user, UserDTO.class))
      .orElseThrow(() -> new EntityNotFound("No user exist with the username " + username));
  }

  @Transactional(rollbackFor = { EntityNotFound.class, ValidationException.class })
  @Override
  public void updatePassword(final UUID id, final EditPasswordDTO editPassword)
      throws EntityNotFound, ValidationException
  {
    log.debug("Updating the password of the user {}", id);

    AppUser user = dao.read(id);
    ValidationResults validationResults = new ValidationResults();

    if (!passwordEncoder.matches(editPassword.getOldPassword(), user.getPassword()))
    {
      validationResults.addError(
          fieldError("oldPassword", getFullCode("password.old-password-mismatch"),
              "The old password must match the user's current password"));
    }
    if (passwordEncoder.matches(editPassword.getPassword(), user.getPassword()))
    {
      validationResults.addError(
          fieldError("password", getFullCode("password.new-password-match"),
              "The new password must not match the user's current password"));
    }

    ValidationResults passwordValidation = passwordValidator.validate("password", editPassword.getPassword(), true);
    validationResults.mergeFieldErrors(passwordValidation);
    validationResults.throwIfError("Update password: errors found");

    log.debug("No validation error found. Updating the password of {}", user);
    user.setPassword(passwordEncoder.encode(editPassword.getPassword()));
    AppUser updatedUser = dao.save(user);

    log.info("Password of {} updated", () -> dtoMapper.toDto(updatedUser, UserDTO.class));
  }

  @Transactional(readOnly = true)
  @Override
  public Optional<BinaryDataDTO> getImage(final UUID id) throws EntityNotFound
  {
    AppUser entity = dao.read(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.getPicture(), BinaryDataDTO.class));
  }

  @Transactional(rollbackFor = { EntityNotFound.class })
  @Override
  public UserDTO uploadImage(final UUID id, final BinaryDataDTO picture) throws EntityNotFound
  {
    log.debug("Uploading a picture for the user {}", id);

    AppUser entity = dao.read(id);

    BinaryData pictureToUpload = dtoMapper.toEntity(picture, BinaryData.class);
    BinaryData savedPicture = binaryDataDao.save(pictureToUpload);
    entity.setPicture(savedPicture);

    AppUser updatedEntity = dao.save(entity);

    log.info("New picture uploaded for the user {}", id);

    return dtoMapper.toDto(updatedEntity, UserDTO.class);
  }
}
