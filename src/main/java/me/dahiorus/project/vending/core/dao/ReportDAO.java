package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

import me.dahiorus.project.vending.core.model.Report;
import me.dahiorus.project.vending.core.model.VendingMachine;

@NoRepositoryBean
public interface ReportDAO extends DAO<Report>
{
  Optional<Report> findLastGenerated(final VendingMachine machine);
}
