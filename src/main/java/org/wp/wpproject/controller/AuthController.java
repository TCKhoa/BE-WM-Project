package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.service.UserService;
import org.wp.wpproject.config.JwtUtil;

/**
 * Controller xử lý đăng nhập và tạo JWT token.
 */
@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Endpoint đăng nhập.
     * @param request DTO chứa email + password
     * @return JWT token + thông tin cơ bản user
     */
    @PostMapping("/login")
    public ResponseEntity<?> createToken(@RequestBody AuthRequest request) {
        try {
            // Xác thực email và password (Spring Security sẽ dùng PasswordEncoder)
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Sai email hoặc mật khẩu");
        }

        // Lấy thông tin user từ DB
        User user = userService.findByEmail(request.getEmail());

        // Tạo JWT token có role
        String jwt = jwtUtil.generateToken(user);

        // Trả về token + thông tin cơ bản của user
        return ResponseEntity.ok(new AuthResponse(jwt, user.getEmail(), user.getUsername(), user.getRole()));
    }
}

/**
 * DTO cho request đăng nhập
 */
class AuthRequest {
    private String email;
    private String password;

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}

/**
 * DTO cho response trả về sau khi đăng nhập
 */
class AuthResponse {
    private String token;
    private String email;
    private String username;
    private String role;

    public AuthResponse(String token, String email, String username, String role) {
        this.token = token;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public String getToken() { return token; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public String getRole() { return role; }
}
