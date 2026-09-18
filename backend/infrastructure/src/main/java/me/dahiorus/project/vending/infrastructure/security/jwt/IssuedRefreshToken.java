package me.dahiorus.project.vending.infrastructure.security.jwt;

import java.time.Instant;

public record IssuedRefreshToken(String value, String jti, Instant expiresAt) {}
