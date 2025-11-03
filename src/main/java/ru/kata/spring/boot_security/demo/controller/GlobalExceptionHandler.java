package ru.kata.spring.boot_security.demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.ui.Model;
import org.springframework.validation.BindException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.service.RoleService;
import ru.kata.spring.boot_security.demo.service.UserService;


 // перехватывает ошибки @Valid/@ModelAttribute (BindException)
 // и возвращает ту же вью с заполненной моделью
 // привязан к AdminController и UserController

@ControllerAdvice(assignableTypes = {
        AdminController.class,
        UserController.class
})
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final UserService userService;
    private final RoleService roleService;

    @ExceptionHandler(BindException.class)
    public String handleBind(BindException ex, Model model, HttpServletRequest req) {
        String objectName = ex.getBindingResult().getObjectName();
        Object target = ex.getTarget();

        model.addAttribute(objectName, target);
        model.addAttribute("org.springframework.validation.BindingResult." + objectName, ex.getBindingResult());

        String uri = req.getRequestURI();

        // страница админки
        if (uri.startsWith("/admin")) {
            // таблица пользователей и список ролей
            model.addAttribute("users", userService.findAll());
            model.addAttribute("roles", roleService.getAllRoles());

            if ("newUser".equals(objectName)) {
                model.addAttribute("editUser", new UserUpdateDto());
            } else if ("editUser".equals(objectName)) {
                model.addAttribute("newUser", new UserCreateDto());
            }
            return "admin";
        }

        // если когда-нибудь появятся формы на /user, можно подложить нужные атрибуты здесь
        return "user";
    }
}
