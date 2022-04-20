package me.dahiorus.project.vending.web.api.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;

public class ProvisionRequest
{
  @Getter
  @Setter
  @JsonProperty(required = true)
  private long quantity;

  @JsonCreator
  public ProvisionRequest(final long quantity)
  {
    this.quantity = quantity;
  }
}
