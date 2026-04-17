package ru.kata.spring.boot_security.demo.controller;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.kata.spring.boot_security.demo.dto.RoleDto;
import ru.kata.spring.boot_security.demo.dto.UserDto;
import ru.kata.spring.boot_security.demo.dto.UserRequestDto;
import ru.kata.spring.boot_security.demo.model.Role;
import ru.kata.spring.boot_security.demo.model.User;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminRestController {

    private final UserService userService;
    private final RoleService roleService;

    public AdminRestController(UserService userService, RoleService roleService) {
        this.userService = userService;
        this.roleService = roleService;
    }

    @GetMapping("/users")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/users/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return toUserDto(userService.getUserById(id));
    }

    @GetMapping("/roles")
    public List<RoleDto> getAllRoles() {
        return roleService.getAllRoles().stream()
                .map(this::toRoleDto)
                .sorted(Comparator.comparing(RoleDto::getName))
                .collect(Collectors.toList());
    }

    @PostMapping("/users")
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@RequestBody UserRequestDto userRequestDto) {
        userService.createUser(
                userRequestDto.getUsername(),
                userRequestDto.getPassword(),
                userRequestDto.getName(),
                userRequestDto.getLastname(),
                userRequestDto.getAge(),
                userRequestDto.getRoles()
        );

        return toUserDto(userService.getAllUsers().stream()
                .filter(user -> user.getUsername().equals(userRequestDto.getUsername()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Created user not found")));
    }

    @PutMapping("/users/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserRequestDto userRequestDto) {
        userService.updateUser(
                id,
                userRequestDto.getUsername(),
                userRequestDto.getPassword(),
                userRequestDto.getName(),
                userRequestDto.getLastname(),
                userRequestDto.getAge(),
                userRequestDto.getRoles()
        );

        User updatedUser = userService.getUserById(id);
        refreshAuthenticationIfCurrentUserWasUpdated(updatedUser);
        return toUserDto(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    private UserDto toUserDto(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User not found");
        }

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

    private void refreshAuthenticationIfCurrentUserWasUpdated(User updatedUser) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof User)) {
            return;
        }

        User currentUser = (User) authentication.getPrincipal();
        if (!currentUser.getId().equals(updatedUser.getId())) {
            return;
        }

        UsernamePasswordAuthenticationToken updatedAuthentication =
                new UsernamePasswordAuthenticationToken(
                        updatedUser,
                        authentication.getCredentials(),
                        updatedUser.getAuthorities()
                );
        updatedAuthentication.setDetails(authentication.getDetails());
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
    }
}
