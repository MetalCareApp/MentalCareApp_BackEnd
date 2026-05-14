package com.remind.remind.domain.user;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 100)
    private String phone;

    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public User(String email, String password, String name, Role role, String phone, LocalDate birthDate, Gender gender) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public void promoteToDoctor() {
        this.role = Role.DOCTOR;
    }

    public boolean isDoctor() {
        return this.role == Role.DOCTOR;
    }

    public boolean isUser() {
        return this.role == Role.USER;
    }
}
