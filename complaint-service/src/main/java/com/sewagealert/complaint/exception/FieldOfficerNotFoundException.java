package com.sewagealert.complaint.exception;

// FieldOfficerNotFoundException: Thrown when the target of an assignment does not exist
// in AUTH-SERVICE — mapped to HTTP 404.
public class FieldOfficerNotFoundException extends RuntimeException {

    public FieldOfficerNotFoundException(String message) {
        super(message);
    }
}
