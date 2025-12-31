package com.techcorp.employee.security;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa użytkownika jest wymagana")
    @Size(min = 3, max = 50, message = "Nazwa użytkownika musi mieć od 3 do 50 znaków")
    @Column(unique = true, nullable = false, length = 50)
    private String username;

    @NotBlank(message = "Hasło jest wymagane")
    @Column(nullable = false, length = 100)
    private String password;

    @NotBlank(message = "Rola jest wymagana")
    @Column(nullable = false, length = 20)
    private String role;

    // Konstruktory
    public AppUser() {}

    public AppUser(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Gettery i settery
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}