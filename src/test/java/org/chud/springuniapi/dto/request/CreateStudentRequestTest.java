package org.chud.springuniapi.dto.request;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

public class CreateStudentRequestTest {
    private static ValidatorFactory factory; //used to create validator objects
    private static Validator validator; //the object actually doing the validation (using the annotations from the dtos

    //get ValidatorFactory once before all tests because its a heavy operation and set teh validator
    @BeforeAll
    static void initValidator(){
        factory = Validation.buildDefaultValidatorFactory(); //gets the default validation system configured for the application
        validator = factory.getValidator();;
    }

    //close factory at the end
    @AfterAll
    static void closeValidator() {
        factory.close();
    }

    //transform a set (for non-duplicate) constraint violations into its messages and store them in a list of strings
                                                            //validation error
    private static List<String> messagesOf(Set<? extends ConstraintViolation<?>> violations) {
        return violations.stream().map(ConstraintViolation::getMessage).toList();
    }

    @Test
    @DisplayName("good request")
    void goodRequest(){
        CreateStudentRequest request = new CreateStudentRequest("Ana", "ana@abv.bg");

        Set<ConstraintViolation<CreateStudentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @ParameterizedTest
    @DisplayName("reject non valid emails")
    @ValueSource(strings = {"em @ail.com", "ana@", "@gmail.com", "noemail", "double@@uni.bg"})
    void rejectNonValidEmail(String email){
        CreateStudentRequest request = new CreateStudentRequest("Ana", email);

        Set<ConstraintViolation<CreateStudentRequest>> violations = validator.validate(request);

        assertThat(messagesOf(violations)).contains("must be a valid email");
    }

    @ParameterizedTest(name = "\"{0}\" should be rejected")
    @DisplayName("reject blank names")
    @ValueSource(strings = {" ", "\t", "\n"})
    @NullAndEmptySource
    void rejectBlankName(String name){
        CreateStudentRequest request = new CreateStudentRequest(name, "email@abv.bg");

        Set<ConstraintViolation<CreateStudentRequest>> violations = validator.validate(request);

        assertThat(messagesOf(violations)).contains("name is required");
    }

    @Test
    @DisplayName("boundary test for name length")
    void rejectTooLongName(){
        String name = "a".repeat(121);

        CreateStudentRequest request = new CreateStudentRequest(name, "email@abv.bg");

        Set<ConstraintViolation<CreateStudentRequest>> violations = validator.validate(request);

        assertThat(messagesOf(violations)).contains("name must be at most 120 characters");
    }

    @Test
    @DisplayName("a name of exactly 120 characters is accepted")
    void acceptsNameAtMaxLength() {
        CreateStudentRequest request = new CreateStudentRequest("a".repeat(120), "email@abv.bg");

        Set<ConstraintViolation<CreateStudentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }



}