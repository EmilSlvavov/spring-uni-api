package org.chud.springuniapi.exception;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound (ResourceNotFoundException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Invalid Input"
        );

        problemDetail.setTitle("Resource Not Found");

        return problemDetail;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate (DuplicateResourceException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Conflict with an existing record"
        );

        problemDetail.setTitle("Duplicate Resource");

        return problemDetail;
    }

    //@Valid throws MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {

        //Spring keeps all violations of @Valid in a BindingResult
        // and attaches it to the exception. Entry per failed constraint
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        //You want pairs of Strings, not a list of objects
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed" //Default message is not user friendly
        );

        problemDetail.setTitle("Validation Failed");

        //We add another property to problemDetail and set it to the Map we made
        problemDetail.setProperty("errors", errors);

        return problemDetail;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConflict(OptimisticLockingFailureException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Modification overlapped"
        );

        problemDetail.setTitle("Optimistic Lock Failure");

        return problemDetail;
    }
}
