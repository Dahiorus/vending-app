package me.dahiorus.project.vending.domain.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.transaction.annotation.Transactional;

import me.dahiorus.project.vending.domain.model.AbstractEntity_;
import me.dahiorus.project.vending.domain.model.Report;
import me.dahiorus.project.vending.domain.model.Report_;
import me.dahiorus.project.vending.domain.model.VendingMachine;

public interface ReportDao extends Dao<Report>
{
  @Transactional(readOnly = true)
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
}
