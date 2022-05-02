package me.dahiorus.project.vending.web.api.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateResponse
{
  @Getter
  @Setter
  private String accessToken;

  @Getter
  @Setter
  private String refreshToken;
}
