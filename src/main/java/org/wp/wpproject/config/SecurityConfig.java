package org.wp.wpproject.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * PasswordEncoder dùng để mã hoá mật khẩu.
     * Nên dùng BCryptPasswordEncoder cho môi trường thực tế.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Cung cấp AuthenticationManager để Spring Security
     * có thể xử lý đăng nhập và xác thực.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Cấu hình bảo mật chính
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Tắt CSRF cho REST API
                .cors(cors -> {}) // Cấu hình CORS nếu có WebMvcConfigurer riêng
                .authorizeHttpRequests(auth -> auth
                        // Cho phép truy cập công khai các endpoint quan trọng
                        .requestMatchers(
                                "/uploads/**",
                                "/api/auth/forgot-password",
                                "/api/auth/send-otp",
                                "/api/login",
                                "/api/register",
                                "api/auth/reset-password"
                        ).permitAll()
                        // Chỉ ADMIN mới được truy cập user management
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        // Các request còn lại phải được xác thực
                        .anyRequest().authenticated()
                )
                // Thêm filter JWT vào trước UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
