package me.dahiorus.project.vending.core.service.impl;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.dto.ReportDTO;

@Component
public class ReportDtoValidator extends DtoValidator<Report, ReportDTO>
{
  private static final Logger logger = LogManager.getLogger(ReportDtoValidator.class);

  @Autowired
  public ReportDtoValidator(final AbstractDAO<Report> dao)
  {
    super(dao);
  }

  @Override
  protected Class<ReportDTO> getSupportedClass()
  {
    return ReportDTO.class;
  }

  @Override
  protected void doValidate(final ReportDTO dto, final Errors errors)
  {
    // nothing to validate
    // the DTO is just here to be rendered by the Web service
  }

  @Override
  public Logger getLogger()
  {
    return logger;
  }
}
