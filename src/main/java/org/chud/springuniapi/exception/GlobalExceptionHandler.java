package org.chud.springuniapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //logger which actually does the work
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)              //request for the log, not visible to user
    public ProblemDetail handleNotFound (ResourceNotFoundException ex, HttpServletRequest request) {

        //get the uuid
        String traceId = newTraceId();

        //log logs the warning
        log.warn("[{}] {} {} -> 404: {}", traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                "Invalid Input"
        );

        problemDetail.setTitle("Resource Not Found");
        problemDetail.setProperty("traceId", traceId);
        //spring puts the endpoint in instance field which cannot be switched off but having it empty
        // makes it so its not serialized
        problemDetail.setInstance(URI.create(""));

        return problemDetail;
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ProblemDetail handleDuplicate (DuplicateResourceException ex, HttpServletRequest request) {

        String traceId = newTraceId();

        log.warn("[{}] {} {} -> 409 Conflict: {}", traceId, request.getMethod(), request.getRequestURI(), ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Conflict with an existing record"
        );

        problemDetail.setTitle("Duplicate Resource");
        problemDetail.setProperty("traceId", traceId);//put the id in the user visible response
        problemDetail.setInstance(URI.create(""));

        return problemDetail;
    }

    //@Valid throws MethodArgumentNotValidException
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {

        String traceId = newTraceId();

        //Spring keeps all violations of @Valid in a BindingResult
        // and attaches it to the exception. Entry per failed constraint
        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        //You want pairs of Strings, not a list of objects
        Map<String, String> errors = new LinkedHashMap<>();

        for (FieldError fieldError : fieldErrors) {
            errors.put(fieldError.getField(), fieldError.getDefaultMessage());
        }

        log.warn("[{}] {} {} -> 400 validation failed: {}", traceId, request.getMethod(), request.getRequestURI(), errors);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "Request validation failed" //Default message is not user friendly
        );

        problemDetail.setTitle("Validation Failed");

        //We add another property to problemDetail and set it to the Map we made
        problemDetail.setProperty("errors", errors);
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setInstance(URI.create(""));

        return problemDetail;
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ProblemDetail handleConflict(OptimisticLockingFailureException ex, HttpServletRequest request) {

        String traceId = newTraceId();

        log.warn("[{}] {} {} -> 409 Modification overlapped: {}", traceId, request.getMethod(), request.getRequestURI(),
            ex.getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Modification overlapped"
        );

        problemDetail.setTitle("Optimistic Lock Failure");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setInstance(URI.create(""));

        return problemDetail;
    }

    //new exception handler for triggered by lost race conditions during create and update
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleConstraintViolation(DataIntegrityViolationException ex, HttpServletRequest request){

        String traceId = newTraceId();

        log.warn("[{}] {} {} -> 409 constraint violation: {}", traceId, request.getMethod(), request.getRequestURI(),
            ex.getMostSpecificCause().getMessage());

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "Constraint violation"
        );

        problemDetail.setTitle("Constraint Violation");
        problemDetail.setProperty("traceId", traceId);
        problemDetail.setInstance(URI.create(""));

        return problemDetail;
    }


    //get a uuid for each log
    private static String newTraceId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }
}
