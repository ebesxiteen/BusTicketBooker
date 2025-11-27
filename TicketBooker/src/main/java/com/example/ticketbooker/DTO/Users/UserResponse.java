package com.example.ticketbooker.DTO.Users;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // Lombok tự sinh Constructor đầy đủ
@NoArgsConstructor  // Lombok tự sinh Constructor rỗng
public class UserResponse {
    private int usersCount;
    private List<UserDTO> listUsers;
}