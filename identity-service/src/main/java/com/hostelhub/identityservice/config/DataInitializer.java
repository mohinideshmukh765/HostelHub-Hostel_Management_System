package com.hostelhub.identityservice.config;


import com.hostelhub.identityservice.entity.Role;
import com.hostelhub.identityservice.entity.RoleName;
import com.hostelhub.identityservice.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private final RoleRepository roleRepository;

    @Bean
    CommandLineRunner initializeRoles() {
        return args -> {

            for (RoleName roleName : RoleName.values()) {

                if (roleRepository.findByName(roleName).isEmpty()) {

                    roleRepository.save(
                            Role.builder()
                                    .name(roleName)
                                    .build()
                    );
                }
            }
        };
    }
}