package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.spring.boot_security.demo.dto.RoleDto;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
public class UserRestController {

    @GetMapping("/current")
    public UserDto getCurrentUser(@AuthenticationPrincipal User user) {
        List<RoleDto> roles = user.getRoles().stream()
                .map(this::toRoleDto)
                .sorted(Comparator.comparing(RoleDto::getName))
                .collect(Collectors.toList());

        return new UserDto(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getAge(),
                user.getUsername(),
                roles
        );
    }

    private RoleDto toRoleDto(Role role) {
        return new RoleDto(role.getId(), role.getName());
    }
}
