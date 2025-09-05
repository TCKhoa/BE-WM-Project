package org.wp.wpproject.service;

import org.wp.wpproject.dto.SendOtpRequest;
import org.wp.wpproject.dto.VerifyOtpRequest;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class ForgotPasswordService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Map<String, OtpData> otpStorage;

    @PostConstruct
    public void init() {
        otpStorage = new HashMap<>();
    }

    public ResponseEntity<?> sendOtp(SendOtpRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Email không tồn tại trong hệ thống!"));
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        otpStorage.put(request.getEmail(), new OtpData(otp, LocalDateTime.now().plusMinutes(5)));

        // Gửi qua Gmail
        emailService.sendOtpEmail(request.getEmail(), otp);

        return ResponseEntity.ok(Map.of("message", "OTP đã được gửi qua email!"));
    }

    public ResponseEntity<?> resetPassword(VerifyOtpRequest request) {
        OtpData otpData = otpStorage.get(request.getEmail());

        if (otpData == null || !otpData.getOtp().equals(request.getOtp())) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP không hợp lệ!"));
        }

        if (otpData.getExpiryTime().isBefore(LocalDateTime.now())) {
            return ResponseEntity.badRequest().body(Map.of("message", "OTP đã hết hạn!"));
        }

        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", "Email không tồn tại!"));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        otpStorage.remove(request.getEmail());

        return ResponseEntity.ok(Map.of("message", "Mật khẩu đã được cập nhật!"));
    }

    private static class OtpData {
        private String otp;
        private LocalDateTime expiryTime;

        public OtpData(String otp, LocalDateTime expiryTime) {
            this.otp = otp;
            this.expiryTime = expiryTime;
        }

        public String getOtp() {
            return otp;
        }

        public LocalDateTime getExpiryTime() {
            return expiryTime;
        }
    }
}
