package org.chud.springuniapi.exception;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.http.ProblemDetail;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.chud.springuniapi.logging.CorrelationFilter.TRACE_ID;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private MockHttpServletRequest request;

    @BeforeEach
    void setUp() {
        // The handler has no dependencies, so no Spring context and no mocks are needed.
        handler = new GlobalExceptionHandler();

        // MockHttpServletRequest is a hand written placeholder
        // The handler only reads getMethod() and getRequestURI() for its log line.
        request = new MockHttpServletRequest();
        request.setMethod("GET");
        request.setRequestURI("/api/students/42");
    }

    // MDC is thread local. Without this, a traceId set in one test
    // leaks into every test that runs after it on the same thread.
    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Nested
    @DisplayName("handleNotFound")
    class HandleNotFound {

        @Test
        @DisplayName("returns 404 with the Resource Not Found title")
        void returns404() {
            ProblemDetail problemDetail = handler.handleNotFound(
                    new ResourceNotFoundException("Student", 42L), request);

            // getStatus() is an int, not an HttpStatus
            assertThat(problemDetail.getStatus()).isEqualTo(404);
            assertThat(problemDetail.getTitle()).isEqualTo("Resource Not Found");
            assertThat(problemDetail.getDetail()).isEqualTo("Invalid Input");
        }

        @Test
        @DisplayName("copies the traceId out of MDC into the response")
        void copiesTraceIdFromMdc() {
            MDC.put(TRACE_ID, "trace-abc");

            ProblemDetail problemDetail = handler.handleNotFound(
                    new ResourceNotFoundException("Student", 42L), request);

            assertThat(problemDetail.getProperties()).containsEntry("traceId", "trace-abc");
        }
    }

    @Nested
    @DisplayName("handleDuplicate")
    class HandleDuplicate {

        @Test
        @DisplayName("returns 409 with the Duplicate Resource title")
        void returns409() {
            ProblemDetail problemDetail = handler.handleDuplicate(
                    new DuplicateResourceException("Email 'ana@uni.bg' is already registered"), request);

            assertThat(problemDetail.getStatus()).isEqualTo(409);
            assertThat(problemDetail.getTitle()).isEqualTo("Duplicate Resource");
            assertThat(problemDetail.getDetail()).isEqualTo("Conflict with an existing record");
        }

        @Test
        @DisplayName("copies the traceId out of MDC into the response")
        void copiesTraceIdFromMdc() {
            MDC.put(TRACE_ID, "trace-xyz");

            ProblemDetail problemDetail = handler.handleDuplicate(
                    new DuplicateResourceException("duplicate"), request);

            assertThat(problemDetail.getProperties()).containsEntry("traceId", "trace-xyz");
        }
    }
}
