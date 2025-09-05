package org.wp.wpproject.controller;

import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // ==============================
    // LẤY DANH SÁCH USER CHƯA BỊ XÓA
    // ==============================
    @GetMapping
    public List<User> getAllUsers() {
        return userRepository.findByDeletedAtIsNull(); // ✅ Chỉ lấy user chưa bị xóa
    }

    // ========================================
    // LẤY USER THEO ID (CHỈ USER CHƯA BỊ XÓA)
    // ========================================
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(id);
        return userOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =================
    // TẠO MỚI USER
    // =================
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // Kiểm tra trùng email
        if (userRepository.existsByEmail(user.getEmail())) {
            return ResponseEntity.badRequest().body("Email đã được sử dụng");
        }
        // Kiểm tra trùng số điện thoại
        if (user.getPhone() != null && userRepository.existsByPhone(user.getPhone())) {
            return ResponseEntity.badRequest().body("Số điện thoại đã được sử dụng");
        }

        // Gán ID ngẫu nhiên và thời gian
        user.setId(UUID.randomUUID().toString());
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setDeletedAt(null); // ✅ Mặc định chưa bị xóa

        User savedUser = userRepository.save(user);
        return ResponseEntity.ok(savedUser);
    }

    // ==============================
    // CẬP NHẬT USER (CHỈ USER CHƯA BỊ XÓA)
    // ==============================
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable String id, @RequestBody User userDetails) {
        Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setStaffCode(userDetails.getStaffCode());
        user.setUsername(userDetails.getUsername());
        user.setEmail(userDetails.getEmail());
        user.setPhone(userDetails.getPhone());
        user.setPassword(userDetails.getPassword());
        user.setRole(userDetails.getRole());
        user.setBirthday(userDetails.getBirthday());
        user.setUpdatedAt(LocalDateTime.now()); // ✅ Cập nhật thời gian update

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(updatedUser);
    }

    // ==============================
    // XÓA MỀM USER (SOFT DELETE)
    // ==============================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findByIdAndDeletedAtIsNull(id);
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        user.setDeletedAt(LocalDateTime.now()); // ✅ Đánh dấu đã bị xóa
        userRepository.save(user);

        return ResponseEntity.noContent().build(); // 204 No Content
    }

    // ==================================================
    // LẤY TẤT CẢ USER BAO GỒM CẢ ĐÃ BỊ XÓA (CHO ADMIN)
    // ==================================================
    @GetMapping("/all")
    public List<User> getAllUsersIncludeDeleted() {
        return userRepository.findAll(); // Không bị lọc @Where
    }

    // ================================================
    // KHÔI PHỤC USER ĐÃ XÓA (SET deletedAt = NULL)
    // ================================================
    @PutMapping("/restore/{id}")
    public ResponseEntity<User> restoreUser(@PathVariable String id) {
        Optional<User> userOpt = userRepository.findById(id); // Lấy cả user đã bị xóa
        if (!userOpt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = userOpt.get();
        if (user.getDeletedAt() == null) {
            return ResponseEntity.badRequest().body(user); // User chưa bị xóa
        }

        user.setDeletedAt(null); // Khôi phục
        user.setUpdatedAt(LocalDateTime.now());

        User restoredUser = userRepository.save(user);
        return ResponseEntity.ok(restoredUser);
    }
}
