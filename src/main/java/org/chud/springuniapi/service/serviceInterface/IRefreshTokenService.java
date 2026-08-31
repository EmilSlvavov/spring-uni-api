package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.TokenRotation;

public interface IRefreshTokenService {
    String issueFor(Long userId);

    TokenRotation rotate(String presentKey);

    void revokeSingle(String presentKey);

    void revokeAllFor(Long userId);


}
