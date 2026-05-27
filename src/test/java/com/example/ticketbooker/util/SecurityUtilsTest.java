package com.example.ticketbooker.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.ticketbooker.Entity.CustomOAuth2User;
import com.example.ticketbooker.Entity.CustomUserDetails;
import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Util.SecurityUtils;

class SecurityUtilsTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void isLoggedInReturnsFalseWhenNoAuthentication() {
        SecurityContextHolder.clearContext();
        assertFalse(SecurityUtils.isLoggedIn());
    }

    @Test
    void isLoggedInReturnsTrueWhenAuthenticatedUserPresent() {
        Users user = Users.builder().email("user@example.com").password("pass").role("ROLE_USER").enabled(true).build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails, "creds");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        assertTrue(SecurityUtils.isLoggedIn());
    }

    @Test
    void extractUserReturnsExpectedTypes() {
        Users user = Users.builder().email("user@example.com").build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        assertEquals(user, SecurityUtils.extractUser(customUserDetails));

        OAuth2User delegate = Mockito.mock(OAuth2User.class);
        Mockito.when(delegate.getAttribute("name")).thenReturn("Example");
        Mockito.when(delegate.getAttribute("email")).thenReturn("oauth@example.com");
        CustomOAuth2User oauth2User = new CustomOAuth2User(delegate, user);
        assertEquals(user, SecurityUtils.extractUser(oauth2User));

        assertNull(SecurityUtils.extractUser("anonymousUser"));
    }

    @Test
    void updateAuthenticationRefreshesSecurityContext() {
        Users user = Users.builder().email("user@example.com").password("pass").role("ROLE_ADMIN").enabled(true).build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken("oldPrincipal", "creds");
        SecurityContextHolder.getContext().setAuthentication(authentication);

        boolean updated = SecurityUtils.updateAuthentication(customUserDetails);

        assertTrue(updated);
        Authentication refreshed = SecurityContextHolder.getContext().getAuthentication();
        assertEquals(customUserDetails, refreshed.getPrincipal());
        assertEquals("creds", refreshed.getCredentials());
        assertTrue(refreshed.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN")));
    }
}
