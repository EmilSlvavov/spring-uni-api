package org.chud.springuniapi.service.facade;

import org.chud.springuniapi.dto.TokenRotation;
import org.chud.springuniapi.dto.request.LoginRequest;
import org.chud.springuniapi.dto.request.RefreshTokenRequest;
import org.chud.springuniapi.dto.response.LoginResponse;
import org.chud.springuniapi.exception.InvalidRefreshTokenException;
import org.chud.springuniapi.exception.LoginException;
import org.chud.springuniapi.security.JwtService;
import org.chud.springuniapi.security.MyUserDetails;
import org.chud.springuniapi.security.MyUserDetailsService;
import org.chud.springuniapi.service.serviceInterface.IRefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationFacadeImpl implements IAuthenticationFacade {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final IRefreshTokenService refreshTokenService;
    private final MyUserDetailsService userDetailsService;
    private final long ttl;
    private final long refreshTtl;

    public AuthenticationFacadeImpl(
        AuthenticationManager authenticationManager,
        JwtService jwtService,
        IRefreshTokenService refreshTokenService,
        MyUserDetailsService userDetailsService,
        @Value("${user.jwt.ttl-minutes}") long ttlMinutes,
        @Value("${user.jwt.refresh-ttl-days}") long refreshTtlDays) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
        this.userDetailsService = userDetailsService;
        this.ttl = ttlMinutes * 60;
        this.refreshTtl = refreshTtlDays * 86400;
    }

    @Override
    public LoginResponse login(LoginRequest request) {

        //authenticate the current user
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        //get the principal from the authentication and cast it into myuserdetails
        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        //null check
        if (myUserDetails == null) {
            throw new LoginException("Empty myUserDetails");
        }

        //issue access token and refresh token
        return new LoginResponse(
            jwtService.issue(myUserDetails),
            ttl,
            refreshTokenService.issueFor(myUserDetails.id()),
            refreshTtl);
    }

    @Override
    public LoginResponse refresh(RefreshTokenRequest request) {

        TokenRotation rotation = refreshTokenService.rotate(request.refreshToken());

        MyUserDetails principal = userDetailsService.loadById(rotation.userId())
            .orElseThrow(() -> new InvalidRefreshTokenException("Owner no longer exists"));

        if (!principal.isEnabled()) {
            refreshTokenService.revokeAllFor(rotation.userId());
            throw new InvalidRefreshTokenException("User is suspended");
        }

        return new LoginResponse(
            jwtService.issue(principal),
            ttl,
            rotation.refreshToken(),
            refreshTtl);
    }

    @Override
    public void logout(RefreshTokenRequest request) {
        refreshTokenService.revokeSingle(request.refreshToken());
    }
}
