package me.dahiorus.project.vending.domain;

public interface Creatable<P, D> {
  D create(P toCreate);
}
