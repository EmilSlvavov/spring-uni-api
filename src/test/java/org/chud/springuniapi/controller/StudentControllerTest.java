package org.chud.springuniapi.controller;

import org.chud.springuniapi.dto.request.CreateStudentRequest;
import org.chud.springuniapi.dto.response.CourseSummaryResponse;
import org.chud.springuniapi.dto.response.StudentResponse;
import org.chud.springuniapi.exception.ResourceNotFoundException;
import org.chud.springuniapi.service.serviceInterface.IStudentService;
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

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvcTester mockMvc;

    @MockitoBean
    private IStudentService studentService;

    @Test
    @DisplayName(" get with id returns 200 with student json")
    void getByIdReturnsJsonWithStatusOk() {
        CourseSummaryResponse course = new CourseSummaryResponse(2L, "Databases");
        StudentResponse student =
                new StudentResponse(1L, "Ana", "ana@uni.bg", null, null, List.of(course));

        when(studentService.findById(1L, null)).thenReturn(student);

        MvcTestResult result = mockMvc.get().uri("/api/students/1").exchange();

        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.name").isEqualTo(student.name());
        assertThat(result).bodyJson().extractingPath("$.email").isEqualTo(student.email());
        assertThat(result).bodyJson().extractingPath("$.courses[0].name").isEqualTo(course.name());
    }

    @Test
    @DisplayName("post with a valid body returns status 201")
    void postWithValidReturnsStatusCreated() {
        when(studentService.create(
                any()))
                .thenReturn(new StudentResponse(
                        1L, "Ana", "ana@uni.bg", null, null, List.of()));

        MvcTestResult result = mockMvc.post().uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ana\",\"email\":\"ana@uni.bg\"}").exchange();

        assertThat(result).hasStatus(201);

        ArgumentCaptor<CreateStudentRequest> requestCaptor =
                ArgumentCaptor.forClass(CreateStudentRequest.class);
        verify(studentService).create(requestCaptor.capture());
        CreateStudentRequest captured = requestCaptor.getValue();

        assertThat(captured.name()).isEqualTo("Ana");
        assertThat(captured.email()).isEqualTo("ana@uni.bg");
    }

    @Test
    @DisplayName("not valid email post should respond with status code 400")
    void postWithInvalidEmailReturnsStatusBadRequest() {
        MvcTestResult result = mockMvc.post().uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\",\"email\":\"not-an-email\"}").exchange();

        assertThat(result).hasStatus(400);
        assertThat(result).bodyJson().extractingPath("$.errors.name")
                .isEqualTo("name is required");
        assertThat(result).bodyJson().extractingPath("$.errors.email")
                .isEqualTo("must be a valid email");
        verifyNoInteractions(studentService);
    }

    @Test
    @DisplayName("failing to get an entity due to id not found")
    void getUserNotFound() {
        when(studentService.findById(999L, null))
                .thenThrow(new ResourceNotFoundException("Student", 999L));

        MvcTestResult result = mockMvc.get().uri("/api/students/999").exchange();

        assertThat(result).hasStatus(404);
        assertThat(result).bodyJson().extractingPath("$.title")
                .isEqualTo("Resource Not Found");
        assertThat(result).bodyJson().extractingPath("$.status")
                .isEqualTo(404);
    }

    @Test
    @DisplayName("calling get with no params calls findById(1L, null)")
    void getWithNoParamsCallsFindByIdWithNull() {
        mockMvc.get().uri("/api/students/1").exchange();

        verify(studentService).findById(1L, null);
    }

    @Test
    @DisplayName("calling get with no params calls findById(1L, {deleted})")
    void getWithParamsCallsFindByIdWithParams() {
        mockMvc.get().uri("/api/students/1?deleted=true").exchange();

        verify(studentService).findById(1L, true);
    }

    @Test
    @DisplayName("checking if jsonInclude(NON_NULL) annotation is working")
    void postCheckForJsonInclude() {
        when(studentService.create(
                any()))
                .thenReturn(new StudentResponse(
                        1L, "Ana", "ana@uni.bg", null, null, List.of()));

        MvcTestResult result = mockMvc.post().uri("/api/students")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Ana\",\"email\":\"ana@uni.bg\"}").exchange();

        assertThat(result).hasStatus(201);
        assertThat(result).bodyJson().doesNotHavePath("$.bio");
        assertThat(result).bodyJson().doesNotHavePath("$.dateOfBirth");
    }
}
