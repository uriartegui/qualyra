package com.qasystem.backend.services;

import com.qasystem.backend.dtos.NonConformityDTO;
import com.qasystem.backend.dtos.NonConformityInsertDTO;
import com.qasystem.backend.dtos.NonConformityUpdateDTO;
import com.qasystem.backend.entities.*;
import com.qasystem.backend.repositories.NonConformityRepository;
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
public class NonConformityService {

    private final NonConformityRepository repository;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public NonConformityService(NonConformityRepository repository,
                                OrganizationRepository organizationRepository,
                                UserRepository userRepository) {
        this.repository = repository;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public NonConformityDTO findByIdForCurrentUser(UUID id, User currentUser) {
        NonConformity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonConformity não encontrada"));

        if (entity.isDeleted()
                || !entity.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new ResourceNotFoundException("NonConformity não encontrada");
        }

        return new NonConformityDTO(entity);
    }

    @Transactional
    public NonConformityDTO create(NonConformityInsertDTO dto, User currentUser) {
        Organization org = organizationRepository.findById(currentUser.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Organização não encontrada"));

        if (currentUser.getRole() == null) {
            throw new ForbiddenException("Usuário não tem permissão para criar não conformidade");
        }

        NonConformity entity = new NonConformity();
        entity.setOrganization(org);
        entity.setCreatedBy(currentUser);
        copyInsertDtoToEntity(dto, entity);

        entity = repository.save(entity);
        return new NonConformityDTO(entity);
    }

    @Transactional(readOnly = true)
    public Page<NonConformityDTO> findAllByCurrentUserOrg(User currentUser, Pageable pageable) {
        UUID orgId = currentUser.getOrganization().getId();
        return repository.findByOrganization_IdAndDeletedFalse(orgId, pageable)
                .map(NonConformityDTO::new);
    }

    @Transactional(readOnly = true)
    public Page<NonConformityDTO> findAll(Pageable pageable) {
        return repository.findAll(pageable)
                .map(NonConformityDTO::new);
    }

    @Transactional
    public NonConformityDTO update(UUID id, NonConformityUpdateDTO dto, User currentUser) {
        NonConformity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonConformity não encontrada"));

        if (entity.isDeleted()
                || !entity.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new ResourceNotFoundException("NonConformity não encontrada");
        }

        if (currentUser.getRole() != Role.MEMBER
                && currentUser.getRole() != Role.ADMIN
                && currentUser.getRole() != Role.OWNER) {
            throw new ForbiddenException("Usuário não tem permissão para editar não conformidade");
        }

        copyUpdateDtoToEntity(dto, entity, currentUser);
        entity = repository.save(entity);
        return new NonConformityDTO(entity);
    }

    @Transactional
    public void deleteSoft(UUID id, User currentUser) {
        NonConformity entity = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("NonConformity não encontrada"));

        if (!entity.getOrganization().getId().equals(currentUser.getOrganization().getId())) {
            throw new ForbiddenException("Usuário não pode acessar outra organização");
        }

        if (currentUser.getRole() != Role.ADMIN && currentUser.getRole() != Role.OWNER) {
            throw new ForbiddenException("Usuário não tem permissão para excluir");
        }

        entity.softDelete();
    }

    private void copyInsertDtoToEntity(NonConformityInsertDTO dto, NonConformity entity) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setSeverity(dto.getSeverity());
        entity.setDueDate(dto.getDueDate());
    }

    private void copyUpdateDtoToEntity(NonConformityUpdateDTO dto, NonConformity entity, User currentUser) {
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setCategory(dto.getCategory());
        entity.setSeverity(dto.getSeverity());
        entity.setStatus(dto.getStatus());
        entity.setDueDate(dto.getDueDate());

        if (dto.getAssignedToId() != null) {
            User assignedTo = userRepository.findById(dto.getAssignedToId())
                    .filter(u -> u.getOrganization().getId().equals(currentUser.getOrganization().getId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Usuário atribuído não encontrado na mesma organização"));
            entity.setAssignedTo(assignedTo);
        }
    }

}
