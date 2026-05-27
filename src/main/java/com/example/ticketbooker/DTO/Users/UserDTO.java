package com.example.ticketbooker.DTO.Users;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import com.example.ticketbooker.Util.Enum.Gender;
import com.example.ticketbooker.Util.Enum.UserStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDTO {
    private Integer userId;
    private String fullName;
    private String phone;
    private String address;
    @DateTimeFormat(pattern = "yyyy/MM/dd")
    private LocalDate dateOfBirth;
    private Gender gender;
    private byte[] profilePhoto;
    private UserStatus status;
    private String email;
    private String role;
    private String provider;
    private boolean enabled;   // Trạng thái kích hoạt
    // ------------------------------------------------
}