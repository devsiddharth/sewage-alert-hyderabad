package com.sewagealert.complaint.exception;

/**
 * ImageStorageException: Thrown when the object-storage provider (Cloudinary) fails an
 * upload or delete. Never swallowed — maps to HTTP 500 in GlobalExceptionHandler so the
 * caller is told the image operation failed instead of persisting broken records.
 */
public class ImageStorageException extends RuntimeException {
    public ImageStorageException(String message) {
        super(message);
    }

    public ImageStorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
