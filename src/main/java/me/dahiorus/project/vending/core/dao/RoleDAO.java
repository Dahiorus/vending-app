package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.data.repository.NoRepositoryBean;

import me.dahiorus.project.vending.core.model.AppRole;

@NoRepositoryBean
public interface RoleDAO extends DAO<AppRole>
{
  Optional<AppRole> findByName(String name);
}
