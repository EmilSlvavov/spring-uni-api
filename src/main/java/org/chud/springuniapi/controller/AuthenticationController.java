package org.chud.springuniapi.controller;

import jakarta.validation.Valid;
import org.chud.springuniapi.dto.request.LoginRequest;
import org.chud.springuniapi.dto.request.RegisterRequest;
import org.chud.springuniapi.dto.response.LoginResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.security.JwtService;
import org.chud.springuniapi.security.MyUserDetails;
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

    public AuthenticationController(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            @Value("${user.jwt.ttl-minutes}") long ttlMinutes,
            IUserService userService
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.ttl = ttlMinutes * 60;
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));

        MyUserDetails myUserDetails = (MyUserDetails) authentication.getPrincipal();

        return new LoginResponse(jwtService.issue(myUserDetails), ttl);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }
}
