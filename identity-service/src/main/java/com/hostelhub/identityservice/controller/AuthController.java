package com.hostelhub.identityservice.controller;


import com.hostelhub.identityservice.dto.request.*;
import com.hostelhub.identityservice.dto.response.AuthResponse;
import com.hostelhub.identityservice.dto.response.UserResponse;
import com.hostelhub.identityservice.entity.User;
import com.hostelhub.identityservice.security.CustomUserDetails;
import com.hostelhub.identityservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest request
    ) {
        authService.logout(request);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<AuthResponse.UserInfo> me(Authentication authentication) {
        CustomUserDetails principal = (CustomUserDetails) authentication.getPrincipal();
        User entity = principal.getUser();
        return ResponseEntity.ok(
                AuthResponse.UserInfo.builder()
                        .id(entity.getId())
                        .firstName(entity.getFirstName())
                        .lastName(entity.getLastName())
                        .email(entity.getEmail())
                        .roles(
                                entity.getRoles()
                                        .stream()
                                        .map(r -> r.getName().name())
                                        .toList()
                        )
                        .build()
        );
    }

    /**
     * Creates a warden account. The warden must log in separately via /api/auth/login.
     * Caller must be ADMIN (security enforcement: Stage 3).
     */
    @PostMapping("/admin/wardens")
    public ResponseEntity<UserResponse> createWarden(
            @Valid @RequestBody CreateWardenRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.createWarden(request));
    }

    /**
     * Creates an admin account. The admin must log in separately via /api/auth/login.
     * Caller must be ADMIN (security enforcement: Stage 3).
     */
    @PostMapping("/admin/admins")
    public ResponseEntity<UserResponse> createAdmin(
            @Valid @RequestBody CreateAdminRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(authService.createAdmin(request));
    }

    /**
     * Assigns (or re-assigns) a hostel to a warden.
     * Use hostelId=null to un-assign.
     * Caller must be ADMIN (security enforcement: Stage 3).
     */
    @PatchMapping("/admin/wardens/{wardenId}/hostel")
    public ResponseEntity<UserResponse> updateWardenHostel(
            @PathVariable java.util.UUID wardenId,
            @RequestParam(required = false) java.util.UUID hostelId
    ) {
        return ResponseEntity.ok(authService.updateWardenHostel(wardenId, hostelId));
    }
}