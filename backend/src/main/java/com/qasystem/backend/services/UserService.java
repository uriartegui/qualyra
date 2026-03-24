package com.qasystem.backend.services;

import com.qasystem.backend.dtos.CreateUserDTO;
import com.qasystem.backend.dtos.UpdateUserDTO;
import com.qasystem.backend.dtos.UserDTO;
import com.qasystem.backend.entities.*;
import com.qasystem.backend.repositories.OrganizationRepository;
import com.qasystem.backend.repositories.UserRepository;
import com.qasystem.backend.repositories.exceptions.ResourceNotFoundException;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final OrganizationRepository organizationRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       OrganizationRepository organizationRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.organizationRepository = organizationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void initAdmin() {
        if (userRepository.count() == 0) {
            Organization org = new Organization();
            org.setName("Qualyra SA");
            org.setType(OrganizationType.BUSINESS);
            org.setPlan(OrganizationPlan.FREE);
            org = organizationRepository.save(org);

            User admin = new User();
            admin.setName("Admin Qualyra");
            admin.setEmail("admin@qualyra.dev");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(Role.ADMIN);
            admin.setOrganization(org);
            admin.setActive(true);
            userRepository.save(admin);
            System.out.println("Admin criado!");
        }
    }

    @Transactional(readOnly = true)
    public UserDTO findByIdForOrg(UUID id, UUID orgId) {
        User entity = userRepository.findByIdAndOrganization_Id(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new UserDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<UserDTO> findAllByOrganization(UUID orgId, Pageable pageable) {
        return userRepository.findByOrganization_Id(orgId, pageable)
                .map(UserDTO::new);
    }

    @Transactional(readOnly = true)
    public User findEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public UserDTO findById(UUID id) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User ID " + id + " not found"));
        return new UserDTO(entity);
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    @Transactional
    public UserDTO create(CreateUserDTO dto, User requester) {
        if (requester.getRole() == Role.MEMBER) {
            throw new IllegalArgumentException("MEMBER não pode criar usuários");
        }
        if (requester.getRole() == Role.ADMIN && dto.getRole() != Role.MEMBER) {
            throw new IllegalArgumentException("ADMIN só pode criar MEMBER");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        Organization org = organizationRepository.findById(requester.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));

        User entity = new User();
        entity.setActive(true);
        entity.setOrganization(org);
        copyDtoToEntity(dto, entity);

        entity = userRepository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO update(UUID id, UpdateUserDTO dto, User requester) {
        User entity = userRepository.findByIdAndOrganization_Id(id, requester.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (requester.getRole() != Role.OWNER && dto.getRole() != Role.MEMBER) {
            throw new IllegalArgumentException("Só OWNER pode alterar para ADMIN");
        }

        entity.setName(dto.getName());
        entity.setRole(dto.getRole());
        entity = userRepository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public UserDTO toggleActive(UUID id, User requester) {
        User entity = userRepository.findByIdAndOrganization_Id(id, requester.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        entity.setActive(!entity.isActive());
        entity = userRepository.save(entity);
        return new UserDTO(entity);
    }

    @Transactional
    public void softDelete(UUID id, User requester) {
        User entity = userRepository.findByIdAndOrganization_Id(id, requester.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        entity.setActive(false);
        userRepository.save(entity);
    }

    private void copyDtoToEntity(CreateUserDTO dto, User entity) {
        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));
        entity.setRole(dto.getRole());
    }
}

