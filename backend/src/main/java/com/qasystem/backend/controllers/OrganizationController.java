package com.qasystem.backend.controllers;

import com.qasystem.backend.dtos.OrganizationDTO;
import com.qasystem.backend.dtos.OrganizationUpdateDTO;
import org.springframework.data.domain.Pageable;
import com.qasystem.backend.entities.User;
import com.qasystem.backend.services.OrganizationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/organizations")
public class OrganizationController {

    @Autowired
    private OrganizationService service;

    @GetMapping
    public ResponseEntity<OrganizationDTO> getCurrent(@AuthenticationPrincipal User user) {
        OrganizationDTO dto = service.getByUser(user.getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/all")
    public ResponseEntity<Page<OrganizationDTO>> findAll(Pageable pageable) {
        Page<OrganizationDTO> dto = service.findAll(pageable);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrganizationDTO> findById(@PathVariable UUID id) {
        OrganizationDTO dto = service.findById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping
    public ResponseEntity<OrganizationDTO> update(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody OrganizationUpdateDTO dto
    ) {
        OrganizationDTO updated = service.updateForOwner(
                user.getId(),
                dto,
                user
        );
        return ResponseEntity.ok(updated);
    }
}
