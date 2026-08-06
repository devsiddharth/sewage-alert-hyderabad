package com.sewagealert.notification.exception;

import com.sewagealert.notification.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

// GlobalExceptionHandler: Translates exceptions into the standardized ApiResponse error
// format — identical across all microservices for consistent frontend handling.
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    // handleValidationExceptions: Catches @Valid annotation failures and returns field-level error details
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

    @ExceptionHandler(NotificationNotFoundException.class)
    public ResponseEntity<ApiResponse<String>> handleNotificationNotFound(NotificationNotFoundException ex) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(NotificationProcessingException.class)
    public ResponseEntity<ApiResponse<String>> handleNotificationProcessing(NotificationProcessingException ex) {
        log.warn("Notification processing failure: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(RabbitMqPublishException.class)
    public ResponseEntity<ApiResponse<String>> handleRabbitMqPublish(RabbitMqPublishException ex) {
        log.error("RabbitMQ publish failure", ex);
        return ResponseEntity
                .status(SERVICE_UNAVAILABLE)
                .body(ApiResponse.error("Message broker is unavailable. Please try again later.", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", ex.getMessage()));
    }
}
