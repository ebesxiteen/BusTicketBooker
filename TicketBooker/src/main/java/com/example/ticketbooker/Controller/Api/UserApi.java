package com.example.ticketbooker.Controller.Api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.context.Context;

import com.example.ticketbooker.DTO.Users.ResetPasswordRequest;
import com.example.ticketbooker.DTO.Users.UpdateUserRequest;
import com.example.ticketbooker.DTO.Users.UserDTO;
import com.example.ticketbooker.DTO.Users.UserIdRequest;
import com.example.ticketbooker.DTO.Users.UserResponse;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.UserService;
import com.example.ticketbooker.Service.OutSource.EmailService;
import com.example.ticketbooker.Util.Mapper.UserMapper;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/users")
public class UserApi {

    @Autowired
    private UserService userService;

    // --- CẦN INJECT THÊM CÁC CÁI NÀY ĐỂ XỬ LÝ MẬT KHẨU & EMAIL ---
    @Autowired
    private UserRepo userRepo;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    // -------------------------------------------------------------

    // 1. Xóa User
    @DeleteMapping("/delete")
    public ResponseEntity<?> deleteUser(@RequestBody UserIdRequest request) {
        boolean result = userService.deleteUser(request);
        if (result) {
            return ResponseEntity.ok().body("Deleted successfully");
        } else {
            return ResponseEntity.badRequest().body("Delete failed");
        }
    }

    // 2. Tìm kiếm User
    @GetMapping("/search")
    public ResponseEntity<UserResponse> searchUser(@RequestParam String name) {
        UserResponse response = userService.getAllUserByName(name);
        return ResponseEntity.ok(response);
    }

    // 3. Cập nhật TRẠNG THÁI
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable("id") Integer id, @RequestBody UserDTO userDTO) {
        UserResponse response = userService.getUserById(id);
        if (response != null && response.getUsersCount() > 0) {
            UserDTO existingUser = response.getListUsers().get(0);
            UpdateUserRequest updateRequest = UserMapper.toUpdateDTO(existingUser);
            updateRequest.setStatus(userDTO.getStatus());
            
            userService.updateUser(updateRequest);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 4. Cập nhật QUYỀN HẠN
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable("id") Integer id, @RequestBody UserDTO userDTO) {
        UserResponse response = userService.getUserById(id);
        if (response != null && response.getUsersCount() > 0) {
            UserDTO existingUser = response.getListUsers().get(0);
            UpdateUserRequest updateRequest = UserMapper.toUpdateDTO(existingUser);
            updateRequest.setRole(userDTO.getRole());
            
            userService.updateUser(updateRequest);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // 5. Kiểm tra tài khoản tồn tại (Cho form đăng ký)
    @GetMapping("/exist")
    public ResponseEntity<?> existAccount(@RequestParam(name = "email", required = false) String email) {
        if (email != null && userRepo.findByEmail(email) != null) {
            return ResponseEntity.ok().build(); // Tìm thấy (Đã tồn tại)
        }
        return ResponseEntity.notFound().build(); // Chưa tồn tại
    }

    // 6. Gửi email xác nhận reset mật khẩu
    @PostMapping("/confirm-reset")
    public boolean sendResetEmail(@RequestBody ResetPasswordRequest request) {
        // Tìm user theo email (Lưu ý: DTO ResetPasswordRequest phải dùng field email)
        Users user = userRepo.findByEmail(request.getEmail());
        
        if (user != null) {
            Context context = new Context();
            // Link reset: gọi vào API số 7 bên dưới
            String requestUrl = "http://localhost:8000/api/users/reset-password?userId=" + user.getId() + "&newPassword=" + request.getNewPassword();
            
            context.setVariable("resetLink", requestUrl);
            
            // Gửi email
            return emailService.sendEmail(user.getEmail(), "Reset Password Confirm", "EmailTemplate/ResetPassword", context);
        }
        return false;
    }

    // 7. Xử lý reset mật khẩu (Khi bấm link trong email)
    @GetMapping("/reset-password")
    public void resetPassword(@RequestParam int userId, @RequestParam String newPassword, HttpServletResponse response) {
        try {
            Users user = userRepo.findById(userId).orElse(null);
            
            if (user != null) {
                // Mã hóa mật khẩu mới và lưu
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepo.save(user); 
                
                response.sendRedirect("/auth"); // Chuyển hướng về trang đăng nhập
            } else {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "User not found");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}