package ru.kata.spring.boot_security.demo.dto;

import java.util.List;

// Плоское безопасное представление пользователя для UI
public record UserViewDto(
        Long id,
        String username,
        String email,
        int age,
        List<String> roles
) {}