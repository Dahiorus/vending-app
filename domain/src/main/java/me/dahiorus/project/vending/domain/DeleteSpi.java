package me.dahiorus.project.vending.domain;

public interface DeleteSpi<I extends DomainId> {
  void delete(I id);
}
