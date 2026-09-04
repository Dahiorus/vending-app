package me.dahiorus.project.vending.domain;

public interface Deletable<I extends DomainId> {
  void delete(I id);
}
