package com.qasystem.backend.services;

import com.qasystem.backend.dtos.OrganizationDTO;
import com.qasystem.backend.dtos.OrganizationUpdateDTO;
import com.qasystem.backend.entities.*;
import com.qasystem.backend.repositories.OrganizationRepository;
import com.qasystem.backend.repositories.UserRepository;
import com.qasystem.backend.repositories.exceptions.ForbiddenException;
import com.qasystem.backend.repositories.exceptions.ResourceNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;


@Service
public class OrganizationService {

    private final OrganizationRepository repository;
    private final UserRepository userRepository;

    public OrganizationService(OrganizationRepository repository, UserRepository userRepository) {
        this.repository = repository;
        this.userRepository = userRepository;
    }

    @Transactional
    public OrganizationDTO create(String name, OrganizationType type, OrganizationPlan plan) {
        Organization entity = new Organization(name, type, plan);
        entity = repository.save(entity);
        return new OrganizationDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<OrganizationDTO> findAll(Pageable pageable) {
        Page<Organization> result = repository.findAll(pageable);
        return result.map(OrganizationDTO::new);
    }

    @Transactional(readOnly = true)
    public OrganizationDTO findById(UUID id) {
        Organization entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada: " + id));
        return new OrganizationDTO(entity);
    }

    @Transactional(readOnly = true)
    public OrganizationDTO getByUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        return new OrganizationDTO(user.getOrganization());
    }

    @Transactional
    public OrganizationDTO update(UUID id, String name) {
        Organization entity = repository.getReferenceById(id);
        entity.setName(name);
        entity = repository.save(entity);
        return new OrganizationDTO(entity);
    }

    @Transactional
    public OrganizationDTO updateForOwner(UUID userId, OrganizationUpdateDTO dto, User requester) {
        if (requester.getRole() != Role.OWNER) {
            throw new ForbiddenException("Somente OWNER pode editar organização");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Organization entity = repository.findById(user.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));
        copyDtoToEntity(dto, entity);
        entity = repository.save(entity);
        return new OrganizationDTO(entity);
    }

    private void copyDtoToEntity(OrganizationUpdateDTO dto, Organization entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setLogoUrl(dto.getLogoUrl());
    }
}