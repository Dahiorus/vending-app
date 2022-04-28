package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;

@Service
public class UserDtoServiceImpl extends DtoServiceImpl<User, UserDTO, UserDAO> implements UserDtoService
{
  private static final Logger logger = LogManager.getLogger(UserDtoServiceImpl.class);

  public UserDtoServiceImpl(final UserDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<User, UserDTO> dtoValidator)
  {
    super(dao, dtoMapper, dtoValidator);
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }

  @Override
  protected Class<UserDTO> getDomainClass()
  {
    return UserDTO.class;
  }
}
