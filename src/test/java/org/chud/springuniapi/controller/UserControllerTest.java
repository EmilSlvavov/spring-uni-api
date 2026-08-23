package org.chud.springuniapi.controller;

import org.chud.springuniapi.dto.request.CreateUserRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.UserResponse;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.service.serviceInterface.IUserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.assertj.MvcTestResult;
import static org.mockito.ArgumentMatchers.any;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private IUserService userService;

    @Test
    @DisplayName(" get with id returns 200 with user json")
    void getByIdReturnsJsonWithStatusOk() {
        CourseSummaryResponse course = new CourseSummaryResponse(2L, "Databases");
        UserResponse user =
                new UserResponse(1L, "Ana", "ana@uni.bg", null, null, List.of(course));

        when(userService.findById(1L, null)).thenReturn(user);

        MvcTestResult result = mockMvc.get().uri("/api/users/1").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo(user.name());
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(user.email());
        assertThat(result).bodyJson().extractingPath("$.courses[0].name").isEqualTo(course.name());
    }

    @Test
    @DisplayName("post with a valid body returns status 201")
    void postWithValidReturnsStatusCreated() {
        when(userService.create(
                any()))
                .thenReturn(new UserResponse(
                        1L, "Ana", "ana@uni.bg", null, null, List.of()));

        MvcTestResult result = mockMvc.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ana\",\"email\":\"ana@uni.bg\"}").exchange();

        assertThat(result).hasStatus(201);

        ArgumentCaptor<CreateUserRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateUserRequest.class);
        verify(userService).create(requestCaptor.capture());
        CreateUserRequest captured = requestCaptor.getValue();

        assertThat(captured.name()).isEqualTo("Ana");
        assertThat(captured.email()).isEqualTo("ana@uni.bg");
    }

    @Test
    @DisplayName("not valid email post should respond with status code 400")
    void postWithInvalidEmailReturnsStatusBadRequest() {
        MvcTestResult result = mockMvc.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"not-an-email\"}").exchange();

        assertThat(result).hasStatus(400);
        assertThat(result).bodyJson().extractingPath("$.errors.name")
                .isEqualTo("name is required");
        assertThat(result).bodyJson().extractingPath("$.errors.email")
                .isEqualTo("must be a valid email");
        verifyNoInteractions(userService);
    }

    @Test
    @DisplayName("failing to get an entity due to id not found")
    void getUserNotFound() {
        when(userService.findById(999L, null))
                .thenThrow(new ResourceNotFoundException("User", 999L));

        MvcTestResult result = mockMvc.get().uri("/api/users/999").exchange();

        assertThat(result).hasStatus(404);
        assertThat(result).bodyJson().extractingPath("$.title")
                .isEqualTo("Resource Not Found");
        assertThat(result).bodyJson().extractingPath("$.status")
                .isEqualTo(404);
    }

    @Test
    @DisplayName("calling get with no params calls findById(1L, null)")
    void getWithNoParamsCallsFindByIdWithNull() {
        mockMvc.get().uri("/api/users/1").exchange();

        verify(userService).findById(1L, null);
    }

    @Test
    @DisplayName("calling get with no params calls findById(1L, {deleted})")
    void getWithParamsCallsFindByIdWithParams() {
        mockMvc.get().uri("/api/users/1?deleted=true").exchange();

        verify(userService).findById(1L, true);
    }

    @Test
    @DisplayName("checking if jsonInclude(NON_NULL) annotation is working")
    void postCheckForJsonInclude() {
        when(userService.create(
                any()))
                .thenReturn(new UserResponse(
                        1L, "Ana", "ana@uni.bg", null, null, List.of()));

        MvcTestResult result = mockMvc.post().uri("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ana\",\"email\":\"ana@uni.bg\"}").exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().doesNotHavePath("$.bio");
        assertThat(result).bodyJson().doesNotHavePath("$.dateOfBirth");
    }
}
