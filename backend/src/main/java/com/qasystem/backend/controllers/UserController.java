package com.qasystem.backend.controllers;

import com.qasystem.backend.dtos.CreateUserDTO;
import com.qasystem.backend.dtos.UpdateUserDTO;
import com.qasystem.backend.dtos.UserDTO;
import com.qasystem.backend.entities.User;
import com.qasystem.backend.services.UserService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> findById(
            @AuthenticationPrincipal User requester,
            @PathVariable UUID id
    ) {
        UserDTO dto = userService.findByIdForOrg(id, requester.getOrganization().getId());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<UserDTO>> findAll(
            @AuthenticationPrincipal User requester,
            @PageableDefault(size = 20, sort = "name") Pageable pageable
    ) {
        Page<UserDTO> dto = userService.findAllByOrganization(requester.getOrganization().getId(), pageable);
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    public ResponseEntity<UserDTO> create(
            @AuthenticationPrincipal User requester,
            @Valid @RequestBody CreateUserDTO dto
    ) {
        UserDTO result = userService.create(dto, requester);
        URI uri = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(result.getId()).toUri();
        return ResponseEntity.created(uri).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDTO> update(
            @AuthenticationPrincipal User requester,
            @PathVariable UUID id,
            @Valid @RequestBody UpdateUserDTO dto
    ) {
        UserDTO result = userService.update(id, dto, requester);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserDTO> toggleStatus(
            @AuthenticationPrincipal User requester,
            @PathVariable UUID id
    ) {
        UserDTO result = userService.toggleActive(id, requester);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal User requester,
            @PathVariable UUID id
    ) {
        userService.softDelete(id, requester);
        return ResponseEntity.noContent().build();
    }
}
