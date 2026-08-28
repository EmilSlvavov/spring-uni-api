package org.chud.springuniapi.service.serviceInterface;

import org.chud.springuniapi.dto.RotationResult;
import org.chud.springuniapi.entity.User;

public interface IRefreshTokenService {
    String issueFor(Long userId);

    RotationResult rotate(String presentKey);

    void revokeSingle(String presentKey);

    void revokeAllFor(Long userId);


}
