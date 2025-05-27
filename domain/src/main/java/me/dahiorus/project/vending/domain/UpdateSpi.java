package me.dahiorus.project.vending.domain;

public interface UpdateSpi<P, D> {
  D update(P toUpdate);
}
