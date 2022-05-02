package me.dahiorus.project.vending.web.api.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
public class AuthenticateResponse
{
  @Schema(description = "Token to use to authorise the user")
  @Getter
  @Setter
  private String accessToken;

  @Schema(description = "Token to use to request a new access token for the user")
  @Getter
  @Setter
  private String refreshToken;
}
