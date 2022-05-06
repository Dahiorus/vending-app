package me.dahiorus.project.vending.core.dao;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import me.dahiorus.project.vending.core.model.AppRole;

@Repository
public interface RoleDAO extends DAO<AppRole>
{
  Optional<AppRole> findByName(String name);
}
