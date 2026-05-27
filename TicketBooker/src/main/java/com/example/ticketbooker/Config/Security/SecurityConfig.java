package com.example.ticketbooker.Config.Security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
@EnableMethodSecurity
public class SecurityConfig {

        private final CustomOAuthUserService customOAuthUserService;
        private final CustomUserDetailsService customUserDetailsService;
        private final CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;
        private final CustomAccessDeniedHandler customAccessDeniedHandler;

        public SecurityConfig(
                        CustomOAuthUserService customOAuthUserService,
                        CustomUserDetailsService customUserDetailsService,
                        CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler,
                        CustomAccessDeniedHandler customAccessDeniedHandler) {
                this.customOAuthUserService = customOAuthUserService;
                this.customUserDetailsService = customUserDetailsService;
                this.customAuthenticationSuccessHandler = customAuthenticationSuccessHandler;
                this.customAccessDeniedHandler = customAccessDeniedHandler;
        }

        @Bean
        public BCryptPasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public DaoAuthenticationProvider authenticationProvider() {
                DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
                authProvider.setUserDetailsService(customUserDetailsService);
                authProvider.setPasswordEncoder(passwordEncoder());
                return authProvider;
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
                return authConfig.getAuthenticationManager();
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(AbstractHttpConfigurer::disable)
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/",
                                                                "/greenbus/**",
                                                                "/auth/**",
                                                                "/register",
                                                                "/login",
                                                                "/logout",
                                                                "/access-denied",
                                                                "/404",
                                                                "/error",
                                                                "/css/**",
                                                                "/js/**",
                                                                "/images/**",
                                                                "/webjars/**",
                                                                "/components/**",
                                                                "/vnpay/**",
                                                                "/zalopay/**",
                                                                "/payment/**")
                                                .permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/trips/search-trip").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/trips/*").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/seats/*/booked").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/routes/search").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/routes/getDepartureLocation").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/routes/getArrivalLocation").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/routes/get-routes").permitAll()
                                                .requestMatchers(HttpMethod.POST, "/api/tickets/payment-infor").permitAll()

                                                .requestMatchers("/admin/**").hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers("/api/users/**", "/api/drivers/**", "/api/buses/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/routes/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.PATCH, "/api/routes/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.DELETE, "/api/trips/**")
                                                .hasAnyAuthority("ADMIN", "MANAGER")
                                                .requestMatchers(HttpMethod.POST, "/api/invoices/search")
                                                .hasAnyAuthority("ADMIN", "MANAGER")

                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/auth")
                                                .loginProcessingUrl("/perform_login")
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .failureUrl("/auth?error=true")
                                                .permitAll())
                                .oauth2Login(oauth2 -> oauth2
                                                .loginPage("/auth")
                                                .userInfoEndpoint(userInfo -> userInfo
                                                                .userService(customOAuthUserService))
                                                .successHandler(customAuthenticationSuccessHandler)
                                                .failureUrl("/auth?error=true"))
                                .logout(logout -> logout
                                                .logoutUrl("/logout")
                                                .logoutSuccessUrl("/greenbus")
                                                .deleteCookies("JSESSIONID")
                                                .permitAll())
                                .exceptionHandling(ex -> ex
                                                .accessDeniedHandler(customAccessDeniedHandler)
                                                .accessDeniedPage("/access-denied"));

                return http.build();
        }
}
