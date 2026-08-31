package org.chud.springuniapi.security;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//Authentication
class BearerTokenFilterTest extends SecurityWebTestSupport {

    @Test
    @DisplayName("a genuine token authenticates and carries the identity the app trusts")
    void validTokenAuthenticates() {
        when(userService.findById(eq(7L), any())).thenReturn(userResponse());

        //Reaching the controller at all proves the filter parsed the token, read
        //the userId claim and put MyUserDetails in the security context, because
        //the endpoint's self-access rule compares against that id.
        assertThat(get("/api/users/7", tokenFor(7L, "STUDENT"))).hasStatusOk();
    }

    @Test
    @DisplayName("a token signed with another secret is refused")
    void forgedTokenIsRefused() {
        //Well formed, claims say ROLE_ADMIN, signed with a key we do not trust.
        //If this ever returns 200, anyone can mint themselves an admin token.
        String forged = signedWith(
                "a-completely-different-secret-of-sufficient-size",
                1L, "mallory@uni.bg", "ROLE_ADMIN", Instant.now());

        assertThat(get("/api/users", forged)).hasStatus(401);
    }

    @Test
    @DisplayName("an expired token is refused")
    void expiredTokenIsRefused() {
        //Correctly signed, but issued two hours ago with the normal 15 minute
        //window. This short lifetime is the entire reason refresh tokens exist.
        String expired = signedWith(TEST_SECRET, 1L, "root@uni.bg", "ROLE_ADMIN",
                Instant.now().minus(Duration.ofHours(2)));

        assertThat(get("/api/users", expired)).hasStatus(401);
    }
}
