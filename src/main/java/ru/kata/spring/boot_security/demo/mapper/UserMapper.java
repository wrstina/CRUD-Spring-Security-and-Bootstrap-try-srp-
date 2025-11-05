package ru.kata.spring.boot_security.demo.mapper;

import org.springframework.stereotype.Component;
import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.dto.UserViewDto;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;

// адаптер между веб-слоем (DTO) и доменной моделью (User), изолирует контроллер от деталей доменной модели: контроллер видит DTO, а не JPA-сущности
// разделяет ответственность: маппер — только преобразование данных;
// сервис — бизнес-правила (проверка уникальности логина, кодирование пароля, нормализация ролей)
@Component
public class UserMapper {

    // собираем новую сущность из полей create-DTO
    public User fromCreateDto(UserCreateDto dto) {
        User u = new User();
        u.setUsername(dto.getUsername());
        u.setEmail(dto.getEmail());
        u.setAge(dto.getAge());
        return u;
    }

    // обновляем у существующего User только простые поля
    public void merge(UserUpdateDto dto, User target) {
        target.setUsername(dto.getUsername());
        target.setEmail(dto.getEmail());
        target.setAge(dto.getAge());
        // пароль и роли не трогаем — это работа сервиса
    }

    // безопасное представление для UI: пароля нет, роли - список имён без "ROLE_"
    public UserViewDto toViewDto(User u) {
        List<String> roleNames = u.getRoles().stream()
                .map(Role::getName)
                .map(n -> n != null && n.startsWith("ROLE_") ? n.substring(5) : n)
                .sorted()
                .toList();
        return new UserViewDto(u.getId(), u.getUsername(), u.getEmail(), u.getAge(), roleNames);
    }
}

