package ru.kata.spring.boot_security.demo.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class UserUpdateDto { // нет бизнес-логики: только маршрутизация и сборка модели для представления

    @NotBlank(message = "Username cannot be empty")
    private String username;

    private String password; // null или пустой - не менять

    @NotBlank(message = "Email cannot be empty")
    private String email;

    @Min(18) @Max(150)
    private int age;

    private List<Long> roleIds; // null - не менять, empty - очистить

    public UserUpdateDto() {}

    public UserUpdateDto(String username, String password, String email, int age, List<Long> roleIds) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age;
        this.roleIds = roleIds;
    }
}
