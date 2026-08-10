package com.sewagealert.complaint.exception;

// ForbiddenException: Thrown when an authenticated user attempts an operation their
// role does not permit (e.g. a citizen assigning complaints, a field officer updating
// another officer's complaint) — mapped to HTTP 403.
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
