package me.dahiorus.project.vending.domain;

import java.io.Serializable;
import java.util.UUID;

public interface DomainId extends Serializable {
  UUID value();
}
