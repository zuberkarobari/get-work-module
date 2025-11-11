package com.getwork.user.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false, unique = true)
    private String phone;

    private String name;

    @Column(unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role = Role.RIDER;

    private String languagePref = "en";

    @Column(nullable = false)
    private Boolean isActive = true;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();
    private Instant deletedAt;

    private Boolean isPhoneVerified = false;
    private Boolean isEmailVerified = false;

    @Enumerated(EnumType.STRING)
    private Status status = Status.PENDING_VERIFICATION;

    public enum Status {
        PENDING_VERIFICATION,
        ACTIVE,
        SUSPENDED,
        DELETED
    }

    public enum Role {
        ADMIN, RIDER, DRIVER
    }
}
