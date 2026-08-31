package org.chud.springuniapi.security;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.chud.springuniapi.controller.RoleController;
import org.chud.springuniapi.controller.UserController;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.security.authorization.AuthorizationServiceImpl;
import org.chud.springuniapi.service.serviceInterface.IRoleService;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

//Shared setup for the tests
@WebMvcTest(controllers = {UserController.class, RoleController.class})
@Import({SecurityConfig.class, JwtService.class, AuthorizationServiceImpl.class})
abstract class SecurityWebTestSupport {

    protected static final String TEST_SECRET =
            "dGVzdC1vbmx5LXNlY3JldC1uZXZlci11c2VkLWFueXdoZXJlLWVsc2U=";

    @Autowired
    protected MockMvcTester mockMvc;

    @Autowired
    protected JwtService jwtService;

    @MockitoBean
    protected IUserService userService;

    @MockitoBean
    protected IRoleService roleService;

    @MockitoBean
    protected MyUserDetailsService myUserDetailsService;

    protected MvcTestResult get(String uri, String token) {
        return mockMvc.get().uri(uri).header("Authorization", "Bearer " + token).exchange();
    }

    //A genuinely signed token for the given user and role.
    protected String tokenFor(Long id, String role) {
        return jwtService.issue(
            new MyUserDetails(
                id,
                "user" + id + "@uni.bg",
                null,
                List.of(new SimpleGrantedAuthority("ROLE_" + role)),
                true));
    }

// JwtService always uses the current time, so we build the token directly
// when the test needs a specific issue time or a different signing key.
    protected static String signedWith(
            String secret, Long id, String email, String authority, Instant issuedAt) {

        SecretKey key = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("uni-api")
                .issuedAt(issuedAt)
                .expiresAt(issuedAt.plus(Duration.ofMinutes(15)))
                .subject(email)
                .claim("userId", id)
                .claim("roles", List.of(authority))
                .build();

        return encoder.encode(JwtEncoderParameters.from(
                JwsHeader.with(MacAlgorithm.HS256).build(), claims)).getTokenValue();
    }

    protected static UserResponse userResponse() {
        return new UserResponse(7L, "Ana", "ana@uni.bg", null, null, List.of());
    }
}
