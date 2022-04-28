package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;
import me.dahiorus.project.vending.core.dao.UserDAO;
import me.dahiorus.project.vending.core.model.User;
import me.dahiorus.project.vending.core.model.dto.UserDTO;
import me.dahiorus.project.vending.core.service.DtoMapper;
import me.dahiorus.project.vending.core.service.UserDtoService;
import me.dahiorus.project.vending.core.service.validation.DtoValidator;

@Log4j2
@Service
public class UserDtoServiceImpl extends DtoServiceImpl<User, UserDTO, UserDAO> implements UserDtoService
{
  public UserDtoServiceImpl(final UserDAO dao, final DtoMapper dtoMapper,
      final DtoValidator<UserDTO> dtoValidator)
  {
    super(dao, dtoMapper, dtoValidator);
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
}
