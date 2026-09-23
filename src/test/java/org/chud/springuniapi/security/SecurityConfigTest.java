package org.chud.springuniapi.security;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.assertj.MvcTestResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

//Authorization
class SecurityConfigTest extends SecurityWebTestSupport {

    @Test
    @DisplayName("an admin-only endpoint admits ADMIN and refuses STUDENT")
    void roleDecidesAccessToAdminEndpoints() {
        when(userService.findAll(null)).thenReturn(List.of());

        assertThat(get("/api/users", tokenFor(1L, "ADMIN"))).hasStatusOk();

        assertThat(get("/api/users", tokenFor(7L, "STUDENT"))).hasStatus(403);
    }

    @Test
    @DisplayName("a STUDENT reads their own record but not another user's")
    void selfAccessIsScopedToTheOwner() {
        when(userService.findById(eq(7L), any())).thenReturn(userResponse());

        assertThat(get("/api/users/7", tokenFor(7L, "STUDENT"))).hasStatusOk();
        assertThat(get("/api/users/8", tokenFor(7L, "STUDENT"))).hasStatus(403);
    }

    @Test
    @DisplayName("a STUDENT cannot assign roles, not even to themselves")
    void selfAccessDoesNotExtendToRoleAssignment() {

        assertThat(patchRole("/api/users/7/role", tokenFor(7L, "STUDENT"))).hasStatus(403);

        //assignRole moved to the account facade, the authorization rule did not move
        when(userAccountFacade.assignRole(anyLong(), any())).thenReturn(userResponse());
        assertThat(patchRole("/api/users/7/role", tokenFor(1L, "ADMIN"))).hasStatusOk();
    }

    private MvcTestResult patchRole(String uri, String token) {
        return mockMvc.patch().uri(uri)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"PROFESSOR\"}")
                .header("Authorization", "Bearer " + token)
                .exchange();
    }
}
