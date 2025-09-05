package org.wp.wpproject.service;

import org.wp.wpproject.entity.User;
import org.wp.wpproject.exception.DuplicateFieldException;
import org.wp.wpproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private HistoryLogService historyLogService;

    @Autowired
    private PasswordEncoder passwordEncoder; // ✅ BCrypt Password Encoder

    // ==============================
    // Lấy tất cả user chưa bị xóa
    // ==============================
    public List<User> getAllUsers() {
        return userRepository.findByDeletedAtIsNull();
    }

    // ==============================
    // Lấy tất cả user bao gồm đã xóa
    // ==============================
    public List<User> getAllUsersIncludeDeleted() {
        return userRepository.findAll();
    }

    // ==============================
    // Lấy user theo id (chưa bị xóa)
    // ==============================
    public Optional<User> getUserById(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id);
    }

    // ==============================
    // Tạo user mới
    // ==============================
    public User createUser(User user) {
        // Kiểm tra trùng StaffCode, Email, Phone
        if (userRepository.existsByStaffCodeAndDeletedAtIsNull(user.getStaffCode())) {
            throw new DuplicateFieldException("Mã nhân viên đã tồn tại: " + user.getStaffCode());
        }
        if (userRepository.existsByEmailAndDeletedAtIsNull(user.getEmail())) {
            throw new DuplicateFieldException("Email đã được sử dụng: " + user.getEmail());
        }
        if (user.getPhone() != null && userRepository.existsByPhoneAndDeletedAtIsNull(user.getPhone())) {
            throw new DuplicateFieldException("Số điện thoại đã được sử dụng: " + user.getPhone());
        }

        // Sinh UUID và hash password
        user.setId(UUID.randomUUID().toString());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);

        User saved = userRepository.save(user);
        historyLogService.logAction("Tạo user: " + saved.getEmail(), saved);
        return saved;
    }

    // ==============================
    // Cập nhật user (chỉ update user chưa bị xóa)
    // ==============================
    public Optional<User> updateUser(String id, User userDetails) {
        return userRepository.findByIdAndDeletedAtIsNull(id).map(user -> {

            // Kiểm tra trùng StaffCode, Email, Phone (trừ chính nó)
            if (!user.getStaffCode().equals(userDetails.getStaffCode()) &&
                    userRepository.existsByStaffCodeAndDeletedAtIsNull(userDetails.getStaffCode())) {
                throw new DuplicateFieldException("Mã nhân viên đã tồn tại: " + userDetails.getStaffCode());
            }
            if (!user.getEmail().equals(userDetails.getEmail()) &&
                    userRepository.existsByEmailAndDeletedAtIsNull(userDetails.getEmail())) {
                throw new DuplicateFieldException("Email đã được sử dụng: " + userDetails.getEmail());
            }
            if (userDetails.getPhone() != null && !userDetails.getPhone().equals(user.getPhone()) &&
                    userRepository.existsByPhoneAndDeletedAtIsNull(userDetails.getPhone())) {
                throw new DuplicateFieldException("Số điện thoại đã được sử dụng: " + userDetails.getPhone());
            }

            user.setStaffCode(userDetails.getStaffCode());
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());

            // Nếu có mật khẩu mới, hash trước khi lưu
            if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDetails.getPassword()));
            }

            user.setRole(userDetails.getRole());
            user.setBirthday(userDetails.getBirthday());
            user.setUpdatedAt(LocalDateTime.now());

            User updated = userRepository.save(user);
            historyLogService.logAction("Cập nhật user: " + updated.getEmail(), updated);
            return updated;
        });
    }

    // ==============================
    // Xóa mềm user (soft delete)
    // ==============================
    public boolean softDeleteUser(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id).map(user -> {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            historyLogService.logAction("Xóa user: " + user.getEmail(), user);
            return true;
        }).orElse(false);
    }

    // ==============================
    // Khôi phục user đã xóa
    // ==============================
    public Optional<User> restoreUser(String id) {
        return userRepository.findById(id).map(user -> {
            if (user.getDeletedAt() != null) {
                user.setDeletedAt(null);
                user.setUpdatedAt(LocalDateTime.now());
                User restored = userRepository.save(user);
                historyLogService.logAction("Khôi phục user: " + restored.getEmail(), restored);
                return restored;
            }
            return user;
        });
    }

    // ==============================
    // Tìm user theo email (chỉ user chưa bị xóa)
    // ==============================
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found or has been deleted: " + email));
    }

    // ==============================
    // Lấy user hiện tại đang đăng nhập
    // ==============================
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Không tìm thấy user đang đăng nhập");
        }
        String email = authentication.getName();
        return findByEmail(email);
    }
}
