package com.example.ticketbooker.Config.Security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.ticketbooker.Service.ServiceImp.CustomOAuthUserService;
import com.example.ticketbooker.Service.ServiceImp.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Để dùng được @PreAuthorize ở Controller
public class SecurityConfig {

        @Autowired
        private CustomOAuthUserService customOAuthUserService;

        @Autowired
        private CustomUserDetailsService customUserDetailsService;

        @Autowired
        private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler; // Dùng chung cho cả 2

        @Autowired
        private CustomAccessDeniedHandler customAccessDeniedHandler;

    // 1. Password Encoder
        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }
    // 2. Authentication Provider

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

    // 3. Authentication Manager
        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

    // 4. Security Filter Chain
        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Public URLs
                        .requestMatchers("/admin/routes/get-routes",
                        "/", "/greenbus/**", "/auth/**", "/register", "/login", "/logout", 
                        "/access-denied", "/404", "/error",
                        "/css/**", "/js/**", "/images/**", "/webjars/**", "/components/**",
                        "/api/**", "/vnpay/**", "/zalopay/**"
                        ).permitAll()

                        // Admin URLs
                        .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "MANAGER")

                        // Authenticated URLs
                        .anyRequest().authenticated()
                )

                // --- CẤU HÌNH ĐĂNG NHẬP THƯỜNG (LOCAL) ---
                .formLogin(form -> form
                .loginPage("/auth")
                .loginProcessingUrl("/perform_login") // Action trong form html phải trỏ vào đây
                .successHandler(customAuthenticationSuccessHandler) // <--- Dùng Handler chung
                .failureUrl("/auth?error=true")
                .permitAll()
                )

            // --- CẤU HÌNH ĐĂNG NHẬP OAUTH2 (GOOGLE) ---
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/auth")
                        .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuthUserService) // Service xử lý lưu user Google vào DB
                        )
                        .successHandler(customAuthenticationSuccessHandler) // <--- Dùng Handler chung
                        .failureUrl("/auth?error=true")
                )

            // --- LOGOUT ---
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/greenbus")
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )

            // --- XỬ LÝ LỖI ---
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler(customAccessDeniedHandler)
                        .accessDeniedPage("/access-denied")
                );

                return http.build();
        }
}