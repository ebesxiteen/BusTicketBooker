package com.example.ticketbooker.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.example.ticketbooker.DTO.Users.AddUserRequest;
import com.example.ticketbooker.Service.UserService;

@Controller
public class AccessController {

    // 1. Đổi AccountService -> UserService
    @Autowired
    private UserService userService;

    @GetMapping("/admin/**")
    @PreAuthorize("hasRole('MANAGER') or hasRole('ADMIN')") // Thêm ADMIN vào cho chắc
    public String managerDashboard() {
        return "redirect:/admin/users";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        System.out.println("Access denied");
        return "View/Util/404Page";
    }

    @GetMapping("/register")
    public String register() {
        // Trả về view đăng ký (bạn cần có file HTML này, hoặc dùng 404 như cũ để test)
        System.out.println("register page requested");
        return "View/Util/404Page";
    }

    // 2. Sửa logic Đăng ký (Register)
    @PostMapping("/register")
    public ResponseEntity<Object> registerUser(@RequestBody AddUserRequest addUserRequest) {
        System.out.println("Received registration data: " + addUserRequest);
        try {
            // Gọi hàm addUser của UserService (hàm này đã xử lý check trùng email và mã hóa pass)
            boolean isCreated = userService.addUser(addUserRequest);
            
            if (isCreated) {
                return ResponseEntity.ok().build();
            }
            else {
            // addUser trả false -> thường là email đã tồn tại
                return ResponseEntity.status(HttpStatus.CONFLICT).build(); // 409
            }

        } catch (Exception e) {
            e.printStackTrace();
            // Lỗi bất ngờ khác -> 500
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}