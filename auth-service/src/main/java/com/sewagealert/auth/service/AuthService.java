package com.sewagealert.auth.service;

import com.sewagealert.auth.dto.AuthResponse;
import com.sewagealert.auth.dto.FieldOfficerResponse;
import com.sewagealert.auth.dto.LoginRequest;
import com.sewagealert.auth.dto.RegisterRequest;
import com.sewagealert.auth.dto.UserRoleResponse;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse getProfile(Long userId);

    // getFieldOfficers: Lists all users with the FIELD_OFFICER role (admin-only consumers).
    List<FieldOfficerResponse> getFieldOfficers();

    // getUserRoleInfo: Returns a user's identity + role — consumed internally by other
    // microservices for server-side role verification. 404 if the user does not exist.
    UserRoleResponse getUserRoleInfo(Long userId);

}