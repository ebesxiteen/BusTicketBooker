package com.example.ticketbooker.Util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.ticketbooker.Entity.CustomOAuth2User;
import com.example.ticketbooker.Entity.CustomUserDetails;
import com.example.ticketbooker.Entity.Users; // Đổi Account -> Users

public class SecurityUtils {

    public static boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated()
                && !authentication.getPrincipal().equals("anonymousUser");
    }

    // Đổi tên hàm và kiểu trả về: extractAccount -> extractUser
    public static Users extractUser(Object principal) {
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser(); // Sẽ sửa CustomUserDetails ở Bước 2
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser(); // Sẽ sửa CustomOAuth2User ở Bước 3
        }
        return null;
    }

    public static boolean updateAuthentication(CustomUserDetails customUserDetails) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            // Update the Authentication object in the SecurityContext
            authentication = new UsernamePasswordAuthenticationToken(
                    customUserDetails,
                    authentication.getCredentials(),
                    customUserDetails.getAuthorities()
            );
            //Set Authentication mới
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return true;
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
    }
}