package ru.kata.spring.boot_security.demo.service;

import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.dto.UserViewDto;

import java.util.List;

public interface UserService {

    List<UserViewDto> findAll();
    UserViewDto getById(Long id); // возвращаем безопасный DTO вместо сущностей — DTO для UI
    UserViewDto save(UserCreateDto dto); // create принимает CreateDto и возвращает ViewDto
    UserViewDto update(Long id, UserUpdateDto dto); // update принимает id + UpdateDto и возвращает ViewDto
    void delete(Long id);
    UserViewDto findByUsername(String username); // для страницы /user
}