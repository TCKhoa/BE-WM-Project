package org.wp.wpproject.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.exception.DuplicateFieldException;
import org.wp.wpproject.service.UserService;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // ==============================
    // LẤY DANH SÁCH USER CHƯA BỊ XÓA
    // ==============================
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // ========================================
    // LẤY USER THEO ID (CHỈ USER CHƯA BỊ XÓA)
    // ========================================
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable String id) {
        Optional<User> userOpt = userService.getUserById(id);
        return userOpt.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // =================
    // TẠO MỚI USER
    // =================
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        try {
            User savedUser = userService.createUser(user);
            return ResponseEntity.ok(savedUser);
        } catch (DuplicateFieldException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi tạo user: " + e.getMessage());
        }
    }

    // ==============================
    // CẬP NHẬT USER (CHỈ USER CHƯA BỊ XÓA)
    // ==============================
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String id, @RequestBody User userDetails) {
        try {
            Optional<User> updatedUser = userService.updateUser(id, userDetails);
            return updatedUser.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (DuplicateFieldException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi cập nhật user: " + e.getMessage());
        }
    }

    // ==============================
    // XÓA MỀM USER (SOFT DELETE)
    // ==============================
    @DeleteMapping("/{id}")
    public ResponseEntity<?> softDeleteUser(@PathVariable String id) {
        boolean deleted = userService.softDeleteUser(id);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // ==================================================
    // LẤY TẤT CẢ USER BAO GỒM CẢ ĐÃ BỊ XÓA (CHO ADMIN)
    // ==================================================
    @GetMapping("/all")
    public List<User> getAllUsersIncludeDeleted() {
        return userService.getAllUsersIncludeDeleted();
    }

    // ================================================
    // KHÔI PHỤC USER ĐÃ XÓA (SET deletedAt = NULL)
    // ================================================
    @PutMapping("/restore/{id}")
    public ResponseEntity<?> restoreUser(@PathVariable String id) {
        try {
            Optional<User> restoredUser = userService.restoreUser(id);
            return restoredUser.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Lỗi khi khôi phục user: " + e.getMessage());
        }
    }
}
