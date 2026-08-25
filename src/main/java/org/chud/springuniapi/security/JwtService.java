package org.chud.springuniapi.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
public class JwtService {

    //encoder decoder and ttl - for later claim
    private final JwtEncoder encoder;
    private final JwtDecoder decoder;
    private final Duration ttl;

    public JwtService(
            JwtEncoder encoder,
            JwtDecoder decoder,
            @Value("${user.jwt.ttl-minutes}") long ttlMinutes) {
        this.encoder = encoder;
        this.decoder = decoder;
        this.ttl = Duration.ofMinutes(ttlMinutes);
    }

    //issuing
    public String issue(MyUserDetails myUserDetails) {
        Instant now = Instant.now();

        //claims passed to the jwt from the user details
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("uni-api")
                .issuedAt(now)
                .expiresAt(now.plus(ttl))
                .subject(myUserDetails.getUsername())
                .claim("userId", myUserDetails.id())
                .claim("roles", myUserDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .toList())
                .build();

        //header
        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();

        //encode and return
        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }

    //decode
    public Jwt parse(String token) {
        return decoder.decode(token);
    }
}
