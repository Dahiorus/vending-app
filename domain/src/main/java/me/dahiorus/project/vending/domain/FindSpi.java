package me.dahiorus.project.vending.domain;

import java.util.Optional;

public interface FindSpi<I extends DomainId, D> {
  Optional<D> find(I id);
}
