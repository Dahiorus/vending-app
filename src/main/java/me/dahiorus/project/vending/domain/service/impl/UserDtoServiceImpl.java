package me.dahiorus.project.vending.domain.service.impl;

import static me.dahiorus.project.vending.domain.service.validation.FieldValidationError.fieldError;
import static me.dahiorus.project.vending.domain.service.validation.ValidationError.getFullCode;

import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.dao.UserDAO;
import me.dahiorus.project.vending.domain.exception.EntityNotFound;
import me.dahiorus.project.vending.domain.exception.ValidationException;
import me.dahiorus.project.vending.domain.model.AppUser;
import me.dahiorus.project.vending.domain.model.BinaryData;
import me.dahiorus.project.vending.domain.model.dto.BinaryDataDTO;
import me.dahiorus.project.vending.domain.model.dto.EditPasswordDTO;
import me.dahiorus.project.vending.domain.model.dto.UserDTO;
import me.dahiorus.project.vending.domain.service.DtoMapper;
import me.dahiorus.project.vending.domain.service.UserDtoService;
import me.dahiorus.project.vending.domain.service.validation.CrudOperation;
import me.dahiorus.project.vending.domain.service.validation.DtoValidator;
import me.dahiorus.project.vending.domain.service.validation.PasswordValidator;
import me.dahiorus.project.vending.domain.service.validation.ValidationResults;

@Log4j2
@Service
public class UserDtoServiceImpl extends DtoServiceImpl<AppUser, UserDTO, UserDAO>
  implements UserDtoService
{
  private static final String FIELD_PASSWORD = "password";

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

  @Override
  protected ValidationResults validate(final UserDTO dto)
  {
    ValidationResults results = super.validate(dto);

    if (StringUtils.isNotEmpty(dto.getPassword()))
    {
      log.debug("Validating the user's password: {}", dto);
      results.mergeFieldErrors(passwordValidator.validate(FIELD_PASSWORD, dto.getPassword()));
    }

    return results;
  }

  @Override
  protected void doBeforeCallingDao(final AppUser entity, final CrudOperation operation)
  {
    if ((operation == CrudOperation.CREATE || operation == CrudOperation.UPDATE) &&
      StringUtils.isNotEmpty(entity.getPassword()))
    {
      log.debug("Encoding the password of the user {}", entity);
      entity.setEncodedPassword(passwordEncoder.encode(entity.getPassword()));
    }
  }

  @Override
  public UserDTO getByUsername(final String username) throws EntityNotFound
  {
    return dao.findByEmail(username)
      .map(user -> dtoMapper.toDto(user, UserDTO.class))
      .orElseThrow(() -> new EntityNotFound("No user exist with the username " + username));
  }

  @Transactional
  @Override
  public void updatePassword(final UUID id, final EditPasswordDTO editPassword)
    throws EntityNotFound, ValidationException
  {
    log.debug("Updating the password of the user {}", id);

    AppUser user = dao.read(id);
    ValidationResults validationResults = new ValidationResults();

    if (!passwordEncoder.matches(editPassword.oldPassword(), user.getEncodedPassword()))
    {
      validationResults
        .addError(fieldError("oldPassword", getFullCode("password.old-password-mismatch"),
          "The old password must match the user's current password"));
    }
    if (passwordEncoder.matches(editPassword.password(), user.getEncodedPassword()))
    {
      validationResults
        .addError(fieldError(FIELD_PASSWORD, getFullCode("password.new-password-match"),
          "The new password must not match the user's current password"));
    }

    ValidationResults passwordValidation = passwordValidator.validate(FIELD_PASSWORD, editPassword.password());
    validationResults.mergeFieldErrors(passwordValidation);
    validationResults.throwIfError("Update password: errors found");

    log.debug("No validation error found. Updating the password of {}", user);
    user.setEncodedPassword(passwordEncoder.encode(editPassword.password()));
    AppUser updatedUser = dao.save(user);

    log.info("Password of {} updated", () -> dtoMapper.toDto(updatedUser, UserDTO.class));
  }

  @Override
  public Optional<BinaryDataDTO> getImage(final UUID id) throws EntityNotFound
  {
    AppUser entity = dao.read(id);

    return Optional.ofNullable(dtoMapper.toDto(entity.getPicture(), BinaryDataDTO.class));
  }

  @Transactional
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
