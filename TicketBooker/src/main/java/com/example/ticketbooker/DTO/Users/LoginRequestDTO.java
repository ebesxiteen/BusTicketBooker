package com.example.ticketbooker.DTO.Users;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor // Thêm constructor đầy đủ tham số
@NoArgsConstructor  // Thêm constructor rỗng (map JSON)
public class LoginRequestDTO {
    private String email;
    private String password;
}