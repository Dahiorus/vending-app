package me.dahiorus.project.vending.core.manager.impl;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.core.dao.AbstractDAO;
import me.dahiorus.project.vending.core.model.AbstractEntity_;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.Report_;
import me.dahiorus.project.vending.core.model.VendingMachine;

@Repository
public class ReportManager extends GenericManagerImpl<Report>
{
  @Autowired
  public ReportManager(final AbstractDAO<Report> dao)
  {
    super(dao);
  }

  @Override
  public Class<Report> getDomainClass()
  {
    return Report.class;
  }

  @Transactional
  public Optional<Report> findLastGenerated(@Nonnull final VendingMachine machine)
  {
    Page<Report> reports = findAll(PageRequest.of(0, 1, Direction.DESC, AbstractEntity_.CREATED_AT),
        (root, query, cb) -> cb.equal(root.get(Report_.machineSerialNumber), machine.getSerialNumber()));

    if (reports.isEmpty())
    {
      return Optional.empty();
    }

    Report report = reports.getContent()
      .get(0);

    return Optional.of(report);
  }
}
