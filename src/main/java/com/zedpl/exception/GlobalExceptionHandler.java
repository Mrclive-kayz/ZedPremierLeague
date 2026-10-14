package com.zedpl.exception;

import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.URI;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Centralised exception-to-HTTP-response mapping.
 *
 * Uses Spring's RFC 7807 {@link ProblemDetail} as the response format so
 * all error responses have a consistent shape:
 * <pre>
 * {
 *   "type":   "about:blank",
 *   "title":  "Not Found",
 *   "status": 404,
 *   "detail": "Team not found with id: 99",
 *   "instance": "/api/teams/99"
 * }
 * </pre>
 *
 * Stack traces, SQL, passwords and credentials are NEVER included.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final URI BLANK = URI.create("about:blank");

    /* ------------------------------------------------------------------ *
     * 404 — resource not found                                            *
     * ------------------------------------------------------------------ */

    @ExceptionHandler(ResourceNotFoundException.class)
    public ProblemDetail handleNotFound(ResourceNotFoundException ex) {
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(ResourceOwnershipException.class)
    public ProblemDetail handleOwnership(ResourceOwnershipException ex) {
        // Return 404 so callers cannot infer the existence of foreign IDs.
        return problem(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /* ------------------------------------------------------------------ *
     * 400 — validation failures                                           *
     * ------------------------------------------------------------------ */

    /** Handles @Valid failures on @RequestBody parameters. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(e ->
                fieldErrors.put(e.getField(), e.getDefaultMessage()));

        ProblemDetail pd = problem(HttpStatus.BAD_REQUEST, "Validation failed");
        pd.setProperty("errors", fieldErrors);
        return pd;
    }

    /** Handles @Validated / constraint violations on method parameters (path/query params). */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining("; "));
        return problem(HttpStatus.BAD_REQUEST, details);
    }

    /** Handles bad enum / type values in query or path parameters (e.g. ?stream=X). */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '" + ex.getValue()
                + "' for parameter '" + ex.getName() + "'";
        if (ex.getRequiredType() != null && ex.getRequiredType().isEnum()) {
            String allowed = String.join(", ",
                    java.util.Arrays.stream(ex.getRequiredType().getEnumConstants())
                            .map(Object::toString)
                            .toArray(String[]::new));
            message += ". Allowed values: " + allowed;
        }
        return problem(HttpStatus.BAD_REQUEST, message);
    }

    /** Handles illegal argument exceptions thrown in service code. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        return problem(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /* ------------------------------------------------------------------ *
     * 500 — catch-all (details withheld from clients)                     *
     * ------------------------------------------------------------------ */

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleUnexpected(Exception ex) {
        // Log with a framework (SLF4J) in real production; omitted here
        // to avoid adding a logger dependency. Replace with log.error() if desired.
        ProblemDetail pd = problem(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later.");
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }

    /* ------------------------------------------------------------------ */

    private ProblemDetail problem(HttpStatus status, String detail) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
        pd.setType(BLANK);
        pd.setProperty("timestamp", Instant.now().toString());
        return pd;
    }
}
