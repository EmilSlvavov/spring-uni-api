package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.RotationResult;
import org.chud.springuniapi.dto.request.LoginRequest;
import org.chud.springuniapi.dto.request.RefreshTokenRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.response.LoginResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.exception.LoginException;
import org.chud.springuniapi.security.JwtService;
import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.service.serviceInterface.IRefreshTokenService;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final long ttl;
    private final IUserService userService;
    private final IRefreshTokenService refreshTokenService;
    private final long refreshTtl;

    public AuthenticationController(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        @Value("${user.jwt.ttl-minutes}") long ttlMinutes,
        @Value("${user.jwt.refresh-ttl-days}") long refreshTtlDays,
        IUserService userService,
        IRefreshTokenService refreshTokenService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.ttl = ttlMinutes * 60;
        this.userService = userService;
        this.refreshTokenService = refreshTokenService;
        this.refreshTtl = refreshTtlDays * 86400;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        if (myUserDetails == null) {
            throw new LoginException("Empty myUserDetails");
        }
        return new LoginResponse(jwtService.issue(myUserDetails),
            ttl,
            refreshTokenService.issueFor(myUserDetails.id()),
            refreshTtl
        );
    }


    //No need for auth here since this endpoint is called when
    // the access token has already expired
    @PostMapping("/refresh")
    public LoginResponse refresh(@Valid @RequestBody RefreshTokenRequest request) {
        RotationResult result = refreshTokenService.rotate(request.refreshToken());

        return new LoginResponse(
            jwtService.issue(result.principal()), ttl,
            result.refreshToken(), refreshTtl);
    }


    //without logout if somebody gets the refresh token it can have access tokens for a week
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        refreshTokenService.revokeSingle(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }
}
