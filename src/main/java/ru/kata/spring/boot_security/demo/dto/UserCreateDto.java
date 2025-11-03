package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserCreateDto { // нет бизнес-логики: только маршрутизация и сборка модели для представления

    @NotBlank(message = "Username cannot be empty")
    @Size(min = 2, max = 50, message = "Username must be between 2 and 50 characters")
    private String username;

    @NotBlank(message = "Password cannot be empty")
    private String password;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email cannot be empty")
    private String email;

    private int age;

    @NotEmpty(message = "At least one role must be selected")
    private List<Long> roleIds;

    public UserCreateDto() {}

    public UserCreateDto(String username, String password, String email, int age, List<Long> roleIds) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age;
        this.roleIds = roleIds;
    }
}