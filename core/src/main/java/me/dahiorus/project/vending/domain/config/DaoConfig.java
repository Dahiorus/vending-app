package me.dahiorus.project.vending.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import me.dahiorus.project.vending.domain.dao.DAO;
import me.dahiorus.project.vending.domain.dao.impl.AbstractDAO;

@Configuration
@EnableJpaRepositories(repositoryBaseClass = AbstractDAO.class, basePackageClasses = DAO.class)
public class DaoConfig
{
  // empty class
}
