package org.chud.springuniapi.dto;

import java.time.Instant;
import org.chud.springuniapi.security.MyUserDetails;

public record RotationResult(
    MyUserDetails principal,      // what jwtService.issue(...) needs
    String refreshToken,          // the raw replacement, only existence outside the DB
    Instant refreshExpiresAt      // so the client knows when to stop trying
) { }
