package org.chud.springuniapi.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResourceNotFoundExceptionTest {

    // JUnit converts column 2 to Long because the parameter is declared as Long.
    @ParameterizedTest(name = "{0} with id {1}")
    @CsvSource({
            "Student,    42,   Student with id 42 not found",
            "Course,     1,    Course with id 1 not found",
            "Department, 999,  Department with id 999 not found"
    })
    @DisplayName("message is built from the resource name and the id")
    void buildsMessageFromResourceAndId(String resource, Long id, String expectedMessage) {
        ResourceNotFoundException ex = new ResourceNotFoundException(resource, id);

        assertThat(ex.getMessage()).isEqualTo(expectedMessage);
    }

    // Practising the assertThrows shape. assertThrows returns the caught exception,
    @Test
    @DisplayName("assertThrows is throwable and carries its message")
    void isThrowableAndCarriesItsMessage() {
        ResourceNotFoundException ex = assertThrows(
                ResourceNotFoundException.class,
                () -> { throw new ResourceNotFoundException("Student", 42L); }
        );

        assertThat(ex.getMessage()).isEqualTo("Student with id 42 not found");
    }

    @Test
    @DisplayName("assertThatThrownBy same check as a single chain")
    void assertThatThrownByStyle() {
        assertThatThrownBy(() -> { throw new ResourceNotFoundException("Course", 7L); })
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Course with id 7 not found");
    }
}
