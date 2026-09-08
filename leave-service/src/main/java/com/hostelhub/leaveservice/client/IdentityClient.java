package com.hostelhub.leaveservice.client;

import com.hostelhub.leaveservice.dto.response.UserResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/api/internal/users/{id}")
    UserResponse getUserById(
            @PathVariable UUID id
    );

}