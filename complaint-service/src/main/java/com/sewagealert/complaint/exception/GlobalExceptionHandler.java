package com.sewagealert.complaint.exception;

import com.sewagealert.complaint.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Servlet-level per-file upload ceiling — used in the 413 message so it never drifts from config. */
    @Value("${spring.servlet.multipart.max-file-size:10MB}")
    private String maxFileSize;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error("Validation failed", errors));
    }

    @ExceptionHandler(ComplaintNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleComplaintNotFound(ComplaintNotFoundException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserProfileNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleUserProfileNotFound(UserProfileNotFoundException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(FieldOfficerNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleFieldOfficerNotFound(FieldOfficerNotFoundException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<String>> handleForbidden(ForbiddenException ex) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidAssignmentException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidAssignment(InvalidAssignmentException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(UserServiceUnavailableException.class)
    public ResponseEntity<ApiResponse<String>> handleUserServiceUnavailable(UserServiceUnavailableException ex) {
        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(InvalidImageException.class)
    public ResponseEntity<ApiResponse<String>> handleInvalidImage(InvalidImageException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(ImageStorageException.class)
    public ResponseEntity<ApiResponse<String>> handleImageStorage(ImageStorageException ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiResponse<String>> handleMaxUploadSize(MaxUploadSizeExceededException ex) {
        return ResponseEntity
                .status(PAYLOAD_TOO_LARGE)
                .body(ApiResponse.error("Image(s) are too large. Maximum size per image is " + maxFileSize + ".", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", ex.getMessage()));
    }
}
