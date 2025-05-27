package me.dahiorus.project.vending.domain;

public interface CreateSpi<P, D> {
  D create(P toCreate);
}
