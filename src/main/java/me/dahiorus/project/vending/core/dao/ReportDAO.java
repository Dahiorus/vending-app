package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.VendingMachine;

public interface ReportDAO extends DAO<Report>
{
  Optional<Report> findLastGenerated(final VendingMachine machine);
}
