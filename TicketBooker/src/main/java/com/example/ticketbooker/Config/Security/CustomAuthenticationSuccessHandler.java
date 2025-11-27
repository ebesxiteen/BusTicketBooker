package com.example.ticketbooker.Config.Security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.ticketbooker.Entity.CustomOAuth2User;
import com.example.ticketbooker.Entity.CustomUserDetails;
import com.example.ticketbooker.Entity.Users;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component // Nên thêm annotation này
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        Object principal = authentication.getPrincipal();
        Users user = null;

        // -----------------------------------------------------------
        // BƯỚC 1: TRÍCH XUẤT ĐỐI TƯỢNG USER TỪ PRINCIPAL
        // -----------------------------------------------------------
        if (principal instanceof CustomUserDetails userDetails) {
            // Trường hợp 1: Đăng nhập thường (Local)
            user = userDetails.getUser(); // Hàm này đã có trong CustomUserDetails mới
            
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            // Trường hợp 2: Đăng nhập Google/Facebook
            user = customOAuth2User.getUser(); // Hàm này đã có trong CustomOAuth2User mới
            System.out.println("OAuth2 Login Success: " + user.getEmail());
        }

        // -----------------------------------------------------------
        // BƯỚC 2: LƯU SESSION & ĐIỀU HƯỚNG
        // -----------------------------------------------------------
        if (user != null) {
            // Lưu thông tin cơ bản vào Session để dùng ở View/Controller
            request.getSession().setAttribute("userId", user.getId());
            request.getSession().setAttribute("email", user.getEmail());
            request.getSession().setAttribute("fullName", user.getFullName());
            request.getSession().setAttribute("role", user.getRole());

            String role = user.getRole();

            // A. Điều hướng cho ADMIN / MANAGER
            if ("MANAGER".equals(role) || "ADMIN".equals(role)) {
                response.sendRedirect("/admin/users"); // Hoặc /admin/statistics
                return;
            }

            // B. Điều hướng cho USER (Google Login chưa có pass)
            // Kiểm tra nếu là Google mà chưa đặt mật khẩu (pass rỗng hoặc null)
            if ("GOOGLE".equals(user.getProvider()) && (user.getPassword() == null || user.getPassword().isEmpty())) {
                // Nếu bạn đã làm trang tạo mật khẩu thì mở dòng dưới
                // response.sendRedirect("/new-password");
                
                // Tạm thời cho về trang chủ để tránh lỗi 404
                response.sendRedirect("/greenbus"); 
                return;
            }

            // C. Mặc định: Về trang chủ
            response.sendRedirect("/greenbus");
        } else {
            // Trường hợp lỗi không tìm thấy user (hiếm gặp)
            response.sendRedirect("/auth?error=true");
        }
    }
}