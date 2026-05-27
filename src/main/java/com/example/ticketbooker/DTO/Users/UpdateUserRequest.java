package com.example.ticketbooker.DTO.Users;

import java.time.LocalDate;

import com.example.ticketbooker.Util.Enum.Gender;
import com.example.ticketbooker.Util.Enum.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // Lombok tự sinh constructor đầy đủ tham số
@NoArgsConstructor  // Lombok tự sinh constructor rỗng
public class UpdateUserRequest {
    private Integer userId;
    
    // Thông tin cá nhân
    private String fullName;
    private String phone;
    private String email;
    private LocalDate dateOfBirth;
    private String address;
    private Gender gender;
    private byte[] profilePhoto;
    private UserStatus status;

    private String role;
}