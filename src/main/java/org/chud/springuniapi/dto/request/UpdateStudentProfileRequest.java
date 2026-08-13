package org.chud.springuniapi.dto.request;

import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

//used for updating the student's student_profile table
//both bio and dateOfBirth are optional here
//Separate from update student
public record UpdateStudentProfileRequest(
        @Size(max = 2000, message = "bio must be at most 2000 characters")
        String bio,

        @Past(message = "dateOfBirth must be in the past")
        LocalDate dateOfBirth
) { }