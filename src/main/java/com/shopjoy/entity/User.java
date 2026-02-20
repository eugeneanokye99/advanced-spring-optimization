package com.shopjoy.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * The type User.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@BatchSize(size = 20)
public class User implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer id;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "email", unique = true, nullable = false, length = 100)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "user_type", length = 20)
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "oauth_provider", length = 20)
    private String oauthProvider;

    @Column(name = "oauth_provider_id", length = 100)
    private String oauthProviderId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 20)
    @Builder.Default
    private List<CartItem> cartItems = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (userType == null) {
            userType = UserType.CUSTOMER;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
