package com.example.ticketbooker.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.ticketbooker.Entity.Users;
import com.example.ticketbooker.Repository.UserRepo;
import com.example.ticketbooker.Service.ServiceImp.CustomUserDetailsService;
import com.example.ticketbooker.Util.Enum.UserStatus;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepo userRepo;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void loadUserByUsernameReturnsCustomUserDetails() {
        Users user = Users.builder()
                .email("a@example.com")
                .password("encoded")
                .role("USER")
                .userStatus(UserStatus.ACTIVE)
                .enabled(true)
                .build();
        when(userRepo.findByEmail("a@example.com")).thenReturn(user);

        UserDetails details = customUserDetailsService.loadUserByUsername("a@example.com");

        assertEquals("a@example.com", details.getUsername());
        assertEquals("encoded", details.getPassword());
        assertTrue(details.isEnabled());
        assertTrue(details.getAuthorities().stream().anyMatch(authority -> authority.getAuthority().equals("USER")));
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(userRepo.findByEmail("missing@example.com")).thenReturn(null);

        assertThrows(UsernameNotFoundException.class,
                () -> customUserDetailsService.loadUserByUsername("missing@example.com"));
    }
}
