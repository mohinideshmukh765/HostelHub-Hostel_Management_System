package com.hostelhub.identityservice.controller;

import com.hostelhub.identityservice.dto.response.UserResponse;
import com.hostelhub.identityservice.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/internal/users")
@RequiredArgsConstructor
public class InternalUserController {

    private final AuthService authService;

    @GetMapping("/{id}")
    public UserResponse getUser(
            @PathVariable UUID id
    ) {
        return authService.getUser(id);
    }

}