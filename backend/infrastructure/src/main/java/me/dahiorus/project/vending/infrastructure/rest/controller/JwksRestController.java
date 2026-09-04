package me.dahiorus.project.vending.infrastructure.rest.controller;

import static me.dahiorus.project.vending.infrastructure.security.config.WebSecurityConfig.JWKS_PATH;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.http.ResponseEntity.ok;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Exposes the public part of the RSA key pair used to sign access/refresh tokens, as a standard
 * JSON Web Key Set, so that tokens can be verified without sharing a secret.
 */
@Tag(name = "Public")
@RestController
public class JwksRestController {

  private final RSAKey rsaJwk;

  public JwksRestController(final RSAKey rsaJwk) {
    this.rsaJwk = rsaJwk;
  }

  @Operation(description = "Get the JSON Web Key Set used to verify the issued access tokens")
  @GetMapping(value = JWKS_PATH, produces = APPLICATION_JSON_VALUE)
  public ResponseEntity<Map<String, Object>> jwks() {
    return ok(new JWKSet(rsaJwk.toPublicJWK()).toJSONObject());
  }
}
