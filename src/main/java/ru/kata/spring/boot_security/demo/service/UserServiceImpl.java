package ru.kata.spring.boot_security.demo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.kata.spring.boot_security.demo.dto.UserCreateDto;
import ru.kata.spring.boot_security.demo.dto.UserUpdateDto;
import ru.kata.spring.boot_security.demo.dto.UserViewDto;
import ru.kata.spring.boot_security.demo.mapper.UserMapper;
import ru.kata.spring.boot_security.demo.entity.Role;
import ru.kata.spring.boot_security.demo.entity.User;
import ru.kata.spring.boot_security.demo.repository.UserRepository;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

// логика кодирования пароля + нормализация ролей

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Override
    public List<UserViewDto> findAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toViewDto)
                .toList();
    }

    @Override
    public UserViewDto getById(Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: id=" + id));
        return userMapper.toViewDto(u);
    }

    @Transactional
    @Override
    public UserViewDto save(UserCreateDto dto) {
        if (userRepository.existsByUsernameIgnoreCase(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists: " + dto.getUsername());
        }

        // берем данные сущности из простых полей (без пароля и ролей)
        User entity = userMapper.fromCreateDto(dto);

        // пароль обязателен при создании
        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
        entity.setPassword(passwordEncoder.encode(dto.getPassword()));

        // нормализация ролей: null/empty -> ROLE_USER; иначе — по id из dto
        entity.setRoles(normalizeRolesOnCreate(dto));

        entity = userRepository.save(entity); // сохраняем в бд
        return userMapper.toViewDto(entity); // возвращаем безопасное представление
    }

    @Transactional
    @Override
    public UserViewDto update(Long id, UserUpdateDto dto) {
        User entity = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("User not found: id=" + id));

        // проверка уникальности username, если меняется
        String newName = dto.getUsername();
        if (newName != null && !newName.equalsIgnoreCase(entity.getUsername())
                && userRepository.existsByUsernameIgnoreCase(newName)) {
            throw new IllegalArgumentException("Username already exists: " + newName);
        }

        // переносим только простые поля (username/email/age) — без пароля и ролей
        userMapper.merge(dto, entity);

        // пароль меняем только если прислан не пустой
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            entity.setPassword(passwordEncoder.encode(dto.getPassword())); // CHANGED
        }

        // роли: null — не менять; empty — очистить; список id — заменить
        if (dto.getRoleIds() != null) {
            entity.setRoles(normalizeRolesOnUpdate(dto));
        }

        entity = userRepository.save(entity);
        return userMapper.toViewDto(entity);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NoSuchElementException("User not found: id=" + id);
        }
        userRepository.deleteById(id);
    }

    // Security методы

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    @Override
    public UserViewDto findByUsername(String username) {
        User u = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
        return userMapper.toViewDto(u);
    }

    // нормализация ролей при создании: если ничего не пришло — назначаем ROLE_USER
    private Set<Role> normalizeRolesOnCreate(UserCreateDto dto) {
        if (dto.getRoleIds() == null || dto.getRoleIds().isEmpty()) {
            return Set.of(roleService.getRoleByName("ROLE_USER"));
        }
        return dto.getRoleIds().stream()
                .map(roleService::getRoleById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    // нормализация ролей при обновлении:
    // null — оставить как есть (мы сюда не попадём, проверка выше),
    // empty — очистить все роли,
    // иначе — заменить на набор по id
    private Set<Role> normalizeRolesOnUpdate(UserUpdateDto dto) {
        if (dto.getRoleIds().isEmpty()) {
            return Set.of();
        }
        return dto.getRoleIds().stream()
                .map(roleService::getRoleById)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}