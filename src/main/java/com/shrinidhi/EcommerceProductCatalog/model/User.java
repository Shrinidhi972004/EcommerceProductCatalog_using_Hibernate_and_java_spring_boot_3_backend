package com.shrinidhi.EcommerceProductCatalog.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")  // Avoiding reserved keyword issues
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String role; // e.g., ROLE_USER or ROLE_ADMIN

    @Column(name = "security_answer1")
    private String securityAnswer1;  // Answer for "What was the name of your first pet?"

    @Column(name = "security_answer2")
    private String securityAnswer2;  // Answer for "What is the name of your favorite teacher?"

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
    private List<CartItem> cartItems;

    @OneToMany(mappedBy = "user")
    @JsonIgnore
    private List<Order> orders;
}
