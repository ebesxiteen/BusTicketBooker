package com.example.ticketbooker.DTO.Users; // Có thể đổi package nếu muốn

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ResetPasswordRequest {
    private String email;
    
    private String newPassword;
}