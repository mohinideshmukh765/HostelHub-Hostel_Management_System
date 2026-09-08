package com.hostelhub.identityservice.repository;


import com.hostelhub.identityservice.entity.Role;
import com.hostelhub.identityservice.entity.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {

    Optional<Role> findByName(RoleName name);
}