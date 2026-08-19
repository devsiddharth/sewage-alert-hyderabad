package com.sewagealert.community.exception;

import com.sewagealert.community.dto.ApiResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.*;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ApiResponse<String>> handleForbiddenException(ForbiddenException ex) {
        log.warn("Forbidden: {}", ex.getMessage());
        return ResponseEntity
                .status(FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

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

    // ExternalApiException: All custom exceptions wrapping external API failures
    // (NewsApiException, GooglePlacesException, LocationServiceException). Mapped to 502 BAD_GATEWAY.
    @ExceptionHandler(ExternalApiException.class)
    public ResponseEntity<ApiResponse<String>> handleExternalApiException(ExternalApiException ex) {
        log.warn("External API error: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_GATEWAY)
                .body(ApiResponse.error("External service error: " + ex.getMessage(), null));
    }

    // FeignException: Any raw Feign transport failure that slipped past the integration services.
    // The exception body may contain sensitive upstream details — never expose it.
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ApiResponse<String>> handleFeignException(FeignException ex) {
        log.error("Feign call to upstream service failed: status={}, reason={}", ex.status(), ex.getMessage());
        return ResponseEntity
                .status(BAD_GATEWAY)
                .body(ApiResponse.error("Upstream service is temporarily unavailable. Please try again later.", null));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<String>> handleRuntimeException(RuntimeException ex) {
        log.warn("Runtime exception: {}", ex.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(ApiResponse.error(ex.getMessage(), null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<String>> handleGenericException(Exception ex) {
        log.error("Unexpected exception", ex);
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("An unexpected error occurred", ex.getMessage()));
    }
}
