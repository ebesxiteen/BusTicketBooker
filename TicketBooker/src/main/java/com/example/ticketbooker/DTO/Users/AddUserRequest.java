package com.example.ticketbooker.DTO.Users;

import com.example.ticketbooker.Util.Enum.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AddUserRequest {
    private String fullName;
    private String email;
    private String phone;
    private String password;
    private String role;
    private UserStatus status;
}