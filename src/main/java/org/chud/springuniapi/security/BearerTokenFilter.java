package org.chud.springuniapi.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;

public class BearerTokenFilter extends OncePerRequestFilter {

    private static final String PREFIX = "Bearer ";
    private final JwtService jwtService;

    public BearerTokenFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader(HttpHeaders.AUTHORIZATION);

        //no token -> pass it to next filter, there are permitAll() endpoints
        if (header == null || !header.startsWith(PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            //parse after removing the prefix
            Jwt jwt = jwtService.parse(header.substring(PREFIX.length()));

            //get authorities from jwt
            List<SimpleGrantedAuthority> authorities =
                    jwt.getClaimAsStringList("roles")
                            .stream()
                            .map(SimpleGrantedAuthority::new)
                            .toList();


            //populate User Details with a null password, we dont hold password
            //here
            MyUserDetails myUserDetails = new MyUserDetails(
                    jwt.getClaim("userId"),
                    jwt.getSubject(),
                    null,
                    authorities,
                    true
            );

            //create authentication with null password which we pass down for
            //authorization
            var authentication =
                    new UsernamePasswordAuthenticationToken(
                            myUserDetails,
                            null,
                            authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (JwtException ex) {
            logger.warn("Rejected bearer token: " + ex.getMessage());
            SecurityContextHolder.clearContext();
        }

        //pass to next filter
        filterChain.doFilter(request, response);
    }
}
