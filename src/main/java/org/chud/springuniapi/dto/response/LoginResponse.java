package org.chud.springuniapi.dto.response;

public record LoginResponse(
    String token,
    long expiresIn,
    String refreshToken,
    long refreshExpiresIn
) { }
