package ru.kata.spring.boot_security.demo.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import jakarta.persistence.*;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Setter
@Entity
@Table(name = "users")
public class User implements UserDetails { // сущность JPA для ролей с реализацией интерфейса Spring Security UserDetails

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Getter
    @Column(unique = true, nullable = false)
    private String email;

    @Getter
    @Column(nullable = false)
    private int age;

    @Getter
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();


    public User() {}

    public User(String username, String password, String email, int age, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.age = age;
        this.roles = roles;
    }

    // UserDetails методы
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return new HashSet<>(roles);
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return true; }


    @Override
    public String getUsername() { return username; }

    @Override
    public String getPassword() { return password; }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}