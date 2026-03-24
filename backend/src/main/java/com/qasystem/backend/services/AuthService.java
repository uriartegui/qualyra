package com.qasystem.backend.services;

import com.qasystem.backend.dtos.LoginDTO;
import com.qasystem.backend.dtos.RegisterDTO;
import com.qasystem.backend.dtos.UserDTO;
import com.qasystem.backend.entities.*;
import com.qasystem.backend.repositories.OrganizationRepository;
import com.qasystem.backend.repositories.UserRepository;
import com.qasystem.backend.repositories.exceptions.ResourceNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserService userService;
    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserService userService,
                       OrganizationRepository organizationRepository,
                       UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userService = userService;
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> login(LoginDTO dto) {
        User user = userService.findByEmail(dto.getEmail());

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Credenciais inválidas");
        }

        return buildAuthResponse(user);
    }

    @Transactional
    public Map<String, Object> register(RegisterDTO dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email já está em uso");
        }

        Organization org = new Organization();
        copyRegisterDtoToOrg(dto, org);
        org = organizationRepository.save(org);

        User owner = new User();
        owner.setActive(true);
        owner.setOrganization(org);
        copyRegisterDtoToUser(dto, owner);
        owner = userRepository.save(owner);

        return buildAuthResponse(owner);
    }

    private Map<String, Object> buildAuthResponse(User user) {
        String token = jwtService.generateToken(user);
        return Map.of("token", token, "user", new UserDTO(user));
    }

    private void copyRegisterDtoToOrg(RegisterDTO dto, Organization org) {
        org.setName(dto.getOrgName());
        org.setType(OrganizationType.BUSINESS);
        org.setPlan(OrganizationPlan.FREE);
    }

    private void copyRegisterDtoToUser(RegisterDTO dto, User user) {
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(Role.OWNER);
    }
}