package me.dahiorus.project.vending.domain.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import me.dahiorus.project.vending.domain.dao.Dao;
import me.dahiorus.project.vending.domain.dao.impl.AbstractDao;

@Configuration
@EnableJpaRepositories(repositoryBaseClass = AbstractDao.class, basePackageClasses = Dao.class)
public class DaoConfig
{
  // empty class
}
