package me.dahiorus.project.vending.infrastructure.security.jwt;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

/**
 * Provides the RSA key pair used to sign and verify access/refresh tokens, and the Spring
 * Security {@link JwtEncoder}/{@link JwtDecoder} beans built on top of it.
 *
 * <p>When {@code jwt.public-key}/{@code jwt.private-key} are not configured, an ephemeral key
 * pair is generated at startup: tokens issued before a restart become invalid. This is acceptable
 * for development but a real key pair must be configured for any long-lived environment.
 */
@Configuration
class JwtKeysConfig {

  private static final Logger logger = LoggerFactory.getLogger(JwtKeysConfig.class);
  private static final String RSA_ALGORITHM = "RSA";
  private static final int EPHEMERAL_KEY_SIZE = 2048;
  private static final String AUTHORITIES_CLAIM_NAME = "roles";

  @Bean
  RSAKey rsaJwk(final JwtProperties properties) {
    if (isNotBlank(properties.getPublicKey()) && isNotBlank(properties.getPrivateKey())) {
      return readConfiguredKeyPair(properties);
    }

    logger.warn(
        "No RSA key pair configured for JWT signing (jwt.public-key / jwt.private-key); "
            + "generating an ephemeral key pair. Tokens issued before a restart will become "
            + "invalid.");

    return generateEphemeralKeyPair();
  }

  @Bean
  JWKSource<SecurityContext> jwkSource(final RSAKey rsaJwk) {
    var jwkSet = new JWKSet(rsaJwk);

    return (jwkSelector, context) -> jwkSelector.select(jwkSet);
  }

  @Bean
  JwtEncoder jwtEncoder(final JWKSource<SecurityContext> jwkSource) {
    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  JwtDecoder jwtDecoder(final RSAKey rsaJwk, final JwtProperties properties) throws JOSEException {
    NimbusJwtDecoder decoder = NimbusJwtDecoder.withPublicKey(rsaJwk.toRSAPublicKey()).build();
    decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer(properties.getIssuerUri()));

    return decoder;
  }

  @Bean
  JwtAuthenticationConverter jwtAuthenticationConverter() {
    // the "roles" claim already holds fully-qualified authorities (e.g. "ROLE_ADMIN"),
    // produced by Role#asRole - no prefix must be added by the converter.
    var authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthoritiesClaimName(AUTHORITIES_CLAIM_NAME);
    authoritiesConverter.setAuthorityPrefix("");

    var converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);

    return converter;
  }

  private static RSAKey readConfiguredKeyPair(final JwtProperties properties) {
    try {
      RSAPublicKey publicKey = readPublicKey(properties.getPublicKey());
      RSAPrivateKey privateKey = readPrivateKey(properties.getPrivateKey());

      return new RSAKey.Builder(publicKey)
          .privateKey(privateKey)
          .keyID(UUID.randomUUID().toString())
          .build();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Unable to read the configured JWT RSA key pair", e);
    }
  }

  private static RSAPublicKey readPublicKey(final String pem)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var keySpec = new X509EncodedKeySpec(decodePem(pem));

    return (RSAPublicKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePublic(keySpec);
  }

  private static RSAPrivateKey readPrivateKey(final String pem)
      throws NoSuchAlgorithmException, InvalidKeySpecException {
    var keySpec = new PKCS8EncodedKeySpec(decodePem(pem));

    return (RSAPrivateKey) KeyFactory.getInstance(RSA_ALGORITHM).generatePrivate(keySpec);
  }

  private static byte[] decodePem(final String pem) {
    String base64 = pem.replaceAll("-----(BEGIN|END)[^-]+-----", "").replaceAll("\\s", "");

    return Base64.getDecoder().decode(base64);
  }

  private static RSAKey generateEphemeralKeyPair() {
    try {
      KeyPairGenerator generator = KeyPairGenerator.getInstance(RSA_ALGORITHM);
      generator.initialize(EPHEMERAL_KEY_SIZE);
      KeyPair keyPair = generator.generateKeyPair();

      return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
          .privateKey((RSAPrivateKey) keyPair.getPrivate())
          .keyID(UUID.randomUUID().toString())
          .build();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("Unable to generate an ephemeral JWT RSA key pair", e);
    }
  }
}
