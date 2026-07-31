package com.sewagealert.user.dto;


import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
// ApiResponse<T>: Generic wrapper for all API responses — ensures a consistent response format across all microservices
// @param <T> The type of the data payload being returned
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object error;

    public ApiResponse() {}

    // Factory constructor for error responses — the 4th boolean parameter is just a discriminator to avoid type erasure conflicts
    public ApiResponse(boolean success, String message, Object error, boolean isError) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Factory methods
    // success: Creates a success response with a message and data payload
    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    // error: Creates an error response with a message and optional error details
    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(errorDetails);
        return response;
    }

}
