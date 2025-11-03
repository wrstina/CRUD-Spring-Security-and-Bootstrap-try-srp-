package ru.kata.spring.boot_security.demo.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.kata.spring.boot_security.demo.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // закрывает N+1 в таблице
    @Override
    @EntityGraph(attributePaths = "roles")
    List<User> findAll();

    // граф на точечное чтение
    @Override
    @EntityGraph(attributePaths = "roles")
    Optional<User> findById(Long id);

    // перегрузка с графом — удобна и для Security, и для страницы /user
    @EntityGraph(attributePaths = "roles")
    Optional<User> findByUsername(String username);

    boolean existsByUsernameIgnoreCase(String username);
}
