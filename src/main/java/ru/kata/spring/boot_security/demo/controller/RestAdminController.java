package ru.kata.spring.boot_security.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.dto.UserViewDto;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestAdminController {

    private final UserService userService;
    private final RoleService roleService;

    // USERS
    @GetMapping("/users")
    public List<UserViewDto> listUsers() {
        return userService.findAll();
    }

    @GetMapping("/users/{id}")
    public UserViewDto getUser(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping("/users")
    public ResponseEntity<UserViewDto> createUser(@Valid @RequestBody UserCreateDto dto) {
        UserViewDto created = userService.save(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/users/{id}")
    public UserViewDto updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateDto dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }

    // CURRENT USER
    @GetMapping("/users/me")
    public UserViewDto me(Principal principal) {
        return userService.findByUsername(principal.getName());
    }

    // ROLES
    @GetMapping("/roles")
    public List<Role> listRoles() {
        return roleService.getAllRoles(); // отдаем id + name
    }
}