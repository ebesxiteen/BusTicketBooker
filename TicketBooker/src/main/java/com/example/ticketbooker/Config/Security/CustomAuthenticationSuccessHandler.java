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

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        
        System.out.println("----- LOGIN SUCCESS HANDLER TRIGGERED -----");

        // 1. LẤY THÔNG TIN USER
        Object principal = authentication.getPrincipal();
        Users user = null;

        if (principal instanceof CustomUserDetails userDetails) {
            user = userDetails.getUser();
        } else if (principal instanceof CustomOAuth2User customOAuth2User) {
            user = customOAuth2User.getUser();
        }

        // Nếu không lấy được user (hiếm), đá về lỗi
        if (user == null) {
            response.sendRedirect("/greenbus/login?error=true");
            return;
        }

        // 2. LƯU SESSION
        request.getSession().setAttribute("userId", user.getId());
        request.getSession().setAttribute("email", user.getEmail());
        request.getSession().setAttribute("fullName", user.getFullName());
        request.getSession().setAttribute("role", user.getRole());

        // 3. ƯU TIÊN 1: KIỂM TRA REDIRECT URL TỪ FORM
        String redirectUrl = request.getParameter("redirect");
        
        // IN RA CONSOLE ĐỂ KIỂM TRA (Debug)
        System.out.println("Redirect Param Value: " + redirectUrl);

        if (redirectUrl != null && !redirectUrl.trim().isEmpty() && !redirectUrl.equals("null")) {
            System.out.println(">> Redirecting to: " + redirectUrl);
            response.sendRedirect(redirectUrl);
            return; // DỪNG TẠI ĐÂY
        }

        // 4. ƯU TIÊN 2: PHÂN QUYỀN (Nếu không có redirect)
        String role = user.getRole();

        if ("MANAGER".equals(role) || "ADMIN".equals(role)) {
            response.sendRedirect("/admin/users");
            return;
        }

        // 5. MẶC ĐỊNH VỀ TRANG CHỦ
        response.sendRedirect("/greenbus");
    }
}