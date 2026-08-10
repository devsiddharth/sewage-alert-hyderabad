package com.sewagealert.complaint.exception;

// InvalidAssignmentException: Thrown when an assignment violates business rules — e.g.
// the selected user lacks the FIELD_OFFICER role, or the complaint is in a terminal
// state (RESOLVED/REJECTED) and can no longer be (re)assigned. Mapped to HTTP 400.
public class InvalidAssignmentException extends RuntimeException {

    public InvalidAssignmentException(String message) {
        super(message);
    }
}
