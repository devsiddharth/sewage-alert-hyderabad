package com.sewagealert.auth.dto;

public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private Object error;

    // code: Application-specific error code (e.g. EMAIL_NOT_VERIFIED) so clients can
    // branch on failure type without parsing the human-readable message.
    private String code;

    public ApiResponse() {}

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message, Object error, boolean isError) {
        this.success = success;
        this.message = message;
        this.error = error;
    }

    // Factory methods
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message, Object errorDetails) {
        return new ApiResponse<>(false, message, errorDetails, true);
    }

    // errorWithCode: Error response carrying an application-specific error code.
    public static <T> ApiResponse<T> errorWithCode(String code, String message, Object errorDetails) {
        ApiResponse<T> response = new ApiResponse<>(false, message, errorDetails, true);
        response.setCode(code);
        return response;
    }

    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }

    public Object getError() { return error; }
    public void setError(Object error) { this.error = error; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
}
