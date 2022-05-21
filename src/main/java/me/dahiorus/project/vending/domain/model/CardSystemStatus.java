package me.dahiorus.project.vending.domain.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(enumAsRef = true)
public enum CardSystemStatus
{
  ERROR,
  NORMAL;
}
