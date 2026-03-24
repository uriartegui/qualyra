package com.qasystem.backend.repositories;

import com.qasystem.backend.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByOrganization_Id(UUID organizationId, Pageable pageable);

    Optional<User> findByIdAndOrganization_Id(UUID id, UUID organizationId);

    Page<User> findByOrganization_IdAndActiveTrue(UUID organizationId, Pageable pageable);

}
