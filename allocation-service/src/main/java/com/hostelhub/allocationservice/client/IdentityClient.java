package com.hostelhub.allocationservice.client;

import com.hostelhub.allocationservice.dto.external.StudentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "identity-service")
public interface IdentityClient {

    @GetMapping("/api/internal/users/{id}")
    StudentResponse getStudent(
            @PathVariable UUID id
    );

}