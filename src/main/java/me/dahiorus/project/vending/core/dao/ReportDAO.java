package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;

import me.dahiorus.project.vending.core.model.AbstractEntity_;
import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.Report_;
import me.dahiorus.project.vending.core.model.VendingMachine;

public interface ReportDAO extends DAO<Report>
{
  default Optional<Report> findLastGenerated(final VendingMachine machine)
  {
    Page<Report> reports = findAll(
        (root, query, cb) -> cb.equal(root.get(Report_.machineSerialNumber), machine.getSerialNumber()),
        PageRequest.of(0, 1, Direction.DESC, AbstractEntity_.CREATED_AT));

    if (reports.isEmpty())
    {
      return Optional.empty();
    }

    Report report = reports.getContent()
      .get(0);

    return Optional.of(report);
  }

  @Override
  default Class<Report> getDomainClass()
  {
    return Report.class;
  }
}
