package com.hostelhub.identityservice.service.impl;


import com.hostelhub.identityservice.dto.request.*;
import com.hostelhub.identityservice.dto.response.AuthResponse;
import com.hostelhub.identityservice.dto.response.UserResponse;
import com.hostelhub.identityservice.entity.*;
import com.hostelhub.identityservice.exception.ResourceAlreadyExistsException;
import com.hostelhub.identityservice.exception.ResourceNotFoundException;
import com.hostelhub.identityservice.repository.RoleRepository;
import com.hostelhub.identityservice.repository.UserRepository;
import com.hostelhub.identityservice.security.JwtProperties;
import com.hostelhub.identityservice.security.JwtService;
import com.hostelhub.identityservice.service.AuthService;
import com.hostelhub.identityservice.service.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;

    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;

    private final RefreshTokenService refreshTokenService;
    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email " + email
            );
        }

        Role studentRole = roleRepository
                .findByName(RoleName.STUDENT)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role STUDENT not found"
                        ));

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(com.hostelhub.identityservice.entity.UserStatus.ACTIVE)
                .build();

        user.getRoles().add(studentRole);

        User saved = userRepository.save(user);

        // Issue tokens immediately so the registering student can start using the system
        String accessToken = jwtService.generateAccessToken(saved);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(saved);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenExpiration() / 1000)
                .user(
                        AuthResponse.UserInfo.builder()
                                .id(saved.getId())
                                .firstName(saved.getFirstName())
                                .lastName(saved.getLastName())
                                .email(saved.getEmail())
                                .roles(
                                        saved.getRoles()
                                                .stream()
                                                .map(role -> role.getName().name())
                                                .toList()
                                )
                                .build()
                )
                .build();
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String accessToken =
                jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenExpiration() / 1000)



                .user(
                        AuthResponse.UserInfo.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .roles(
                                        user.getRoles()
                                                .stream()
                                                .map(role -> role.getName().name())
                                                .toList()
                                )
                                .build()
                )
                .build();
    }

    @Override
    public AuthResponse refreshToken(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.refreshToken()
                );

        User user = refreshToken.getUser();

        String accessToken =
                jwtService.generateAccessToken(user);

        refreshTokenService.revokeRefreshToken(
                request.refreshToken()
        );

        RefreshToken newRefreshToken =
                refreshTokenService.createRefreshToken(user);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(newRefreshToken.getToken())
                .tokenType("Bearer")
                .expiresIn(jwtProperties.accessTokenExpiration() / 1000)
                .user(
                        AuthResponse.UserInfo.builder()
                                .id(user.getId())
                                .firstName(user.getFirstName())
                                .lastName(user.getLastName())
                                .email(user.getEmail())
                                .roles(
                                        user.getRoles()
                                                .stream()
                                                .map(role -> role.getName().name())
                                                .toList()
                                )
                                .build()
                )
                .build();

    }

    @Transactional
    @Override
    public void logout(LogoutRequest request) {

        refreshTokenService.revokeRefreshToken(
                request.refreshToken()
        );

    }
    @Override
    @Transactional(readOnly = true)
    public UserResponse getStudent(UUID userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "User not found"
                        )
                );

        return new UserResponse(

                user.getId(),

                user.getFirstName(),

                user.getLastName(),

                user.getEmail(),

                user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .toList(),

                user.getStatus() ==
                        com.hostelhub.identityservice.entity.UserStatus.ACTIVE,

                user.getHostelId()


        );
    }

    @Override
    public UserResponse createWarden(CreateWardenRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email " + email
            );
        }

        Role wardenRole = roleRepository
                .findByName(RoleName.WARDEN)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role WARDEN not found"
                        ));

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                // Persist the hostelId if provided so warden-scope checks in
                // complaint-service and leave-service work from day one
                .hostelId(request.hostelId())
                .build();

        user.getRoles().add(wardenRole);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRoles().stream().map(r -> r.getName().name()).toList(),
                saved.getStatus() == UserStatus.ACTIVE,
                saved.getHostelId()
        );
    }

    @Override
    public UserResponse createAdmin(CreateAdminRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResourceAlreadyExistsException(
                    "User already exists with email " + email
            );
        }

        Role adminRole = roleRepository
                .findByName(RoleName.ADMIN)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Role ADMIN not found"
                        ));

        User user = User.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.password()))
                .status(UserStatus.ACTIVE)
                .build();

        user.getRoles().add(adminRole);

        User saved = userRepository.save(user);

        return new UserResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRoles().stream().map(r -> r.getName().name()).toList(),
                saved.getStatus() == UserStatus.ACTIVE,
                saved.getHostelId()
        );
    }


    @Override
    @Transactional
    public UserResponse updateWardenHostel(UUID wardenId, UUID hostelId) {

        User warden = userRepository.findById(wardenId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found with id: " + wardenId));

        boolean isWarden = warden.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleName.WARDEN);

        if (!isWarden) {
            throw new ResourceNotFoundException("No warden found with id: " + wardenId);
        }

        warden.setHostelId(hostelId);
        User saved = userRepository.save(warden);

        return new UserResponse(
                saved.getId(),
                saved.getFirstName(),
                saved.getLastName(),
                saved.getEmail(),
                saved.getRoles().stream().map(r -> r.getName().name()).toList(),
                saved.getStatus() == UserStatus.ACTIVE,
                saved.getHostelId()
        );
    }

    @Override
    public UserResponse getUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoles()
                        .stream()
                        .map(role -> role.getName().name())
                        .toList(),
                user.getStatus() == UserStatus.ACTIVE,
                user.getHostelId()
        );
    }
}