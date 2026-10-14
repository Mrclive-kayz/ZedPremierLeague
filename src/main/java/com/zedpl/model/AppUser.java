package com.zedpl.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

/** Application user, prepared for future Spring Security. Table is "users" ("user" is reserved in PostgreSQL). */
@Entity
@Table(name = "users")
public class AppUser extends AuditedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Email
    @Size(max = 255)
    @Column(nullable = false, unique = true, length = 255)
    private String email;

    /** Must only ever hold a password HASH (e.g. BCrypt), never plain text. */
    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    protected AppUser() { }

    public AppUser(String username, String email, String passwordHash, UserRole role) {
        this.username = username;
        this.email = email;
        this.password = passwordHash;
        this.role = role;
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String passwordHash) { this.password = passwordHash; }
    public UserRole getRole() { return role; }
    public void setRole(UserRole role) { this.role = role; }
}
