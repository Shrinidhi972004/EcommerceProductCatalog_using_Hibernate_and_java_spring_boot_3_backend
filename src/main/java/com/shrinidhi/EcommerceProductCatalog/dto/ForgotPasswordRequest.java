package com.shrinidhi.EcommerceProductCatalog.dto;

import lombok.Data;

@Data
public class ForgotPasswordRequest {
    private String username;
    private String answer1;
    private String answer2;
    private String newPassword;
}
