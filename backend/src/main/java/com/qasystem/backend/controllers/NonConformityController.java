package com.qasystem.backend.controllers;

import com.qasystem.backend.dtos.NonConformityDTO;
import com.qasystem.backend.dtos.NonConformityInsertDTO;
import com.qasystem.backend.dtos.NonConformityUpdateDTO;
import com.qasystem.backend.entities.NonConformity;
import com.qasystem.backend.entities.User;
import com.qasystem.backend.services.NonConformityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.UUID;


@RestController
@RequestMapping("/non-conformities")
public class NonConformityController {

    @Autowired
    private NonConformityService service;

    @PostMapping
    public ResponseEntity<NonConformityDTO> insert(
            @AuthenticationPrincipal User currentUser,
            @Valid @RequestBody NonConformityInsertDTO dto
    ) {
        NonConformityDTO result = service.create(dto, currentUser);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(uri).body(result);
    }

    @GetMapping
    public ResponseEntity<Page<NonConformityDTO>> findAll(
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 20, sort = "createdAt") Pageable pageable
    ) {
        Page<NonConformityDTO> result = service.findAllByCurrentUserOrg(currentUser, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NonConformityDTO> findById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        NonConformityDTO dto = service.findByIdForCurrentUser(id, currentUser);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<NonConformityDTO> update(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id,
            @Valid @RequestBody NonConformityUpdateDTO dto
    ) {
        NonConformityDTO result = service.update(id, dto, currentUser);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id
    ) {
        service.deleteSoft(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}