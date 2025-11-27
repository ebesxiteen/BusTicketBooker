package com.example.ticketbooker.DTO.Users;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder            // Giúp tạo object nhanh: UserIdRequest.builder().userId(1).build()
@AllArgsConstructor // Thay thế constructor có tham số
@NoArgsConstructor  // Thay thế constructor rỗng
public class UserIdRequest {
    private Integer userId; //'private' để đảm bảo tính đóng gói
}