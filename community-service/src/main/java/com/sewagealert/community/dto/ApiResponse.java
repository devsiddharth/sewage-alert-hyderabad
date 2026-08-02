package com.sewagealert.community.dto;

// ApiResponse<T>: Standard response format (consistent with other services)
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object error;

    public ApiResponse() {}

    public static <T> ApiResponse<T> success(String message, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(true);
        response.setMessage(message);
        response.setData(data);
        return response;
    }

    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        ApiResponse<T> response = new ApiResponse<>();
        response.setSuccess(false);
        response.setMessage(message);
        response.setError(errorDetails);
        return response;
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }
}
