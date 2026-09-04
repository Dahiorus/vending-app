package me.dahiorus.project.vending.domain;

public interface Updatable<P, D> {
  D update(P toUpdate);
}
