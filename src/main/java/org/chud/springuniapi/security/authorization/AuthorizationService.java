package org.chud.springuniapi.security.authorization;

import org.springframework.security.core.Authentication;

public interface AuthorizationService {

    boolean isAccessingSelf(Long id, Authentication authentication);

}
