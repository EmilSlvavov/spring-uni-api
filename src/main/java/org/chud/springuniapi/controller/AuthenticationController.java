package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.LoginRequest;
import org.chud.springuniapi.dto.request.RefreshTokenRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.response.LoginResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.service.facade.IAuthenticationFacade;
import org.chud.springuniapi.service.facade.IUserAccountFacade;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final IAuthenticationFacade authenticationFacade;
    private final IUserAccountFacade userAccountFacade;

    public AuthenticationController(
        IAuthenticationFacade authenticationFacade,
        IUserAccountFacade userAccountFacade
    ) {
        this.authenticationFacade = authenticationFacade;
        this.userAccountFacade = userAccountFacade;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authenticationFacade.login(request);
    }


    //No need for auth here since this endpoint is called when
    // the access token has already expired
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return authenticationFacade.refresh(request);
    }


    //without logout if somebody gets the refresh token it can have access tokens for a week
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationFacade.logout(request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userAccountFacade.register(request));
    }
}
