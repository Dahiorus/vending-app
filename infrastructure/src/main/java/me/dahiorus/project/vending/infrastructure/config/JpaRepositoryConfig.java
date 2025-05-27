package me.dahiorus.project.vending.infrastructure.config;

import me.dahiorus.project.vending.infrastructure.jpa.entity.JpaEntity;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EnableJpaRepositories(basePackages = "me.dahiorus.project.vending.infrastructure.jpa.repository")
@EntityScan(basePackageClasses = JpaEntity.class)
public class JpaRepositoryConfig {}
