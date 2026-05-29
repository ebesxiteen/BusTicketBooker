package com.example.ticketbooker.Util;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.ticketbooker.Entity.CustomOAuth2User;
import com.example.ticketbooker.Entity.CustomUserDetails;
import com.example.ticketbooker.Entity.Users;

public class SecurityUtils {

    public static boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }

        Object principal = authentication.getPrincipal();
        if (principal == null || "anonymousUser".equals(principal)) {
            return false;
        }

        return authentication.isAuthenticated()
                || principal instanceof CustomUserDetails
                || principal instanceof CustomOAuth2User;
    }

    public static Users extractUser(Object principal) {
        if (principal instanceof CustomUserDetails customUserDetails) {
            return customUserDetails.getUser();
        }
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User.getUser();
        }
        return null;
    }

    public static boolean updateAuthentication(CustomUserDetails customUserDetails) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || customUserDetails == null) {
            return false;
        }

        Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                authentication.getCredentials(),
                customUserDetails.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(updatedAuthentication);
        return true;
    }
}
