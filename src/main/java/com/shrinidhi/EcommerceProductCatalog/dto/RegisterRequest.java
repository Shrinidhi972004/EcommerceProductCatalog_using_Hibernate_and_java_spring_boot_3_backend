package com.shrinidhi.EcommerceProductCatalog.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    private String username;
    private String password;
    private String role;

    // Add these 👇 for security questions
    private String securityAnswer1; // What was the name of your first pet?
    private String securityAnswer2; // What is the name of your favorite teacher?
}
