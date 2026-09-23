package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.request.LoginRequest;
import org.chud.springuniapi.dto.request.RefreshTokenRequest;
import org.chud.springuniapi.dto.response.LoginResponse;

//Login, refresh and logout each need the refresh token store and the user's
//authentication view at the same time. This is where the two meet, so that
//RefreshTokenServiceImpl never has to touch JPA.
public interface IAuthenticationFacade {

    LoginResponse login(LoginRequest request);

    LoginResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
