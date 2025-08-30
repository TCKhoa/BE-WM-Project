package org.wp.wpproject.service;

import org.wp.wpproject.entity.User;
import org.wp.wpproject.exception.DuplicateFieldException;
import org.wp.wpproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    // ✅ Lấy tất cả user chưa bị xóa
    public List<User> getAllUsers() {
        return userRepository.findByDeletedAtIsNull();
    }

    // ✅ Lấy user theo id (chưa bị xóa)
    public Optional<User> getUserById(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id);
    }

    // ✅ Tạo user mới
    public User createUser(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new DuplicateFieldException("Email đã được sử dụng");
        }
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            throw new DuplicateFieldException("Số điện thoại đã được sử dụng");
        }

        user.setId(UUID.randomUUID().toString());
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        user.setDeletedAt(null);

        User saved = userRepository.save(user);

        // ✅ Ghi log
        historyLogService.logAction("Tạo user: " + saved.getEmail(), saved);

        return saved;
    }

    // ✅ Cập nhật user (chỉ update user chưa bị xóa)
    public Optional<User> updateUser(String id, User userDetails) {
        return userRepository.findByIdAndDeletedAtIsNull(id).map(user -> {
            user.setStaffCode(userDetails.getStaffCode());
            user.setUsername(userDetails.getUsername());
            user.setEmail(userDetails.getEmail());
            user.setPhone(userDetails.getPhone());
            user.setPassword(userDetails.getPassword());
            user.setRole(userDetails.getRole());
            user.setBirthday(userDetails.getBirthday());
            user.setUpdatedAt(LocalDateTime.now());

            User updated = userRepository.save(user);

            // ✅ Ghi log
            historyLogService.logAction("Cập nhật user: " + updated.getEmail(), updated);

            return updated;
        });
    }

    // ✅ Xóa mềm user (đặt deletedAt thay vì xóa hẳn)
    public boolean softDeleteUser(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id).map(user -> {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);

            // ✅ Ghi log
            historyLogService.logAction("Xóa user: " + user.getEmail(), user);

            return true;
        }).orElse(false);
    }

    // ✅ Tìm user theo email
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found or has been deleted: " + email));
    }

    // ✅ Lấy user hiện tại đang đăng nhập (dùng Spring Security)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Không tìm thấy user đang đăng nhập");
        }

        String email = authentication.getName(); // username/email dùng khi login
        return findByEmail(email);
    }
}
