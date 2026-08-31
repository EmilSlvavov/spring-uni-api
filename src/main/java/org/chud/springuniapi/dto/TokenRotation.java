package org.chud.springuniapi.dto;

import java.time.Instant;


public record TokenRotation(
    Long userId, // who the rotated token belongs to
    String refreshToken, // the raw replacement, only existence outside the DB
    Instant refreshExpiresAt //the client knows when to stop trying
) { }
