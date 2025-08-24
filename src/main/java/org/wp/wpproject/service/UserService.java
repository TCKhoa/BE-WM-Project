package org.wp.wpproject.service;

import org.wp.wpproject.entity.User;
import org.wp.wpproject.exception.DuplicateFieldException;
import org.wp.wpproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    // Lấy tất cả user chưa bị ẩn
    public List<User> getAllUsers() {
        return userRepository.findByDeletedAtIsNull();
    }

    // Lấy user theo id và chưa bị ẩn
    public Optional<User> getUserById(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id);
    }

    // Tạo user mới
    public User createUser(User user) {
        // Sinh ID tự động (UUID)

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

        return userRepository.save(user);
    }

    // Cập nhật user (chỉ với user chưa bị ẩn)
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
            return userRepository.save(user);
        });
    }

    // Tìm user theo email
    public User findByEmail(String email) {
        return userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() -> new RuntimeException("User not found or has been deleted: " + email));
    }

    // Soft delete user: đặt deletedAt thay vì xóa
    public boolean softDeleteUser(String id) {
        return userRepository.findByIdAndDeletedAtIsNull(id).map(user -> {
            user.setDeletedAt(LocalDateTime.now());
            userRepository.save(user);
            return true;
        }).orElse(false);
    }
}
