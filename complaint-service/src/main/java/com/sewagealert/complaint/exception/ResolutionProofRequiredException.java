package com.sewagealert.complaint.exception;

/**
 * ResolutionProofRequiredException: Thrown when an admin/authority attempts to mark a
 * complaint as RESOLVED without uploading the mandatory resolution-proof photo. Maps to
 * HTTP 400 in GlobalExceptionHandler with a clear, user-facing message.
 */
public class ResolutionProofRequiredException extends RuntimeException {
    public ResolutionProofRequiredException(String message) {
        super(message);
    }
}
