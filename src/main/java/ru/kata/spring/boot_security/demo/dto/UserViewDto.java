package ru.kata.spring.boot_security.demo.dto;

import java.util.List;

// в JSON не утекает пароль/технические поля. UserViewDto содержит только безопасные данные — id/username/email/age/roles
// стабильный контракт для фронта
public record UserViewDto(
        Long id,
        String username,
        String email,
        int age,
        List<String> roles
) {}