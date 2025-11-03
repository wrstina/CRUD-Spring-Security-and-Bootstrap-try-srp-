package ru.kata.spring.boot_security.demo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping
    public String adminPage(Model model) {
        // только наполняем модель для первого показа страницы.
        model.addAttribute("users", userService.findAll());
        model.addAttribute("roles", roleService.getAllRoles());
        model.addAttribute("newUser", new UserCreateDto());
        model.addAttribute("editUser", new UserUpdateDto());
        return "admin";
    }

    @PostMapping("/users/create")
    public String createUser(
            @ModelAttribute("newUser") @Valid UserCreateDto dto
    ) {
        userService.save(dto);
        return "redirect:/admin";
    }

    @PostMapping("/users/update/{id}")
    public String updateUser(
            @PathVariable Long id,
            @ModelAttribute("editUser") @Valid UserUpdateDto dto
    ) {
        userService.update(id, dto);
        return "redirect:/admin";
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return "redirect:/admin";
    }
}