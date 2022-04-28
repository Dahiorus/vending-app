package me.dahiorus.project.vending.web.api.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.ToString;

@ToString
public class ProvisionRequest
{
  @Getter
  @JsonProperty(required = true)
  private Long quantity;

  @JsonCreator
  public ProvisionRequest(final Long quantity)
  {
    this.quantity = quantity;
  }
}
