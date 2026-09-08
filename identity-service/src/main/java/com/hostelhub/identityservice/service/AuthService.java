package com.hostelhub.identityservice.service;

import com.hostelhub.identityservice.dto.request.*;
import com.hostelhub.identityservice.dto.response.AuthResponse;
import com.hostelhub.identityservice.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(LogoutRequest request);

    UserResponse getStudent(UUID userId);

    /**
     * Creates a warden account. Returns the created user's profile.
     * Wardens log in separately via /api/auth/login.
     */
    UserResponse createWarden(CreateWardenRequest request);

    /**
     * Creates an admin account. Returns the created user's profile.
     * Admins log in separately via /api/auth/login.
     */
    UserResponse createAdmin(CreateAdminRequest request);

    /**
     * Assigns (or re-assigns) a hostel to a warden.
     * Pass {@code null} for hostelId to un-assign the warden from their current hostel.
     */
    UserResponse updateWardenHostel(UUID wardenId, UUID hostelId);

    UserResponse getUser(UUID id);
}