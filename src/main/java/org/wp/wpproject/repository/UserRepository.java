package org.wp.wpproject.repository;

import org.wp.wpproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ==============================
    // Kiểm tra sự tồn tại
    // ==============================
    boolean existsByUsername(String username);             // Kiểm tra username đã tồn tại
    boolean existsByEmail(String email);                   // Kiểm tra email đã tồn tại
    boolean existsByPhone(String phone);                   // Kiểm tra số điện thoại đã tồn tại
    boolean existsByIdAndDeletedAtIsNull(String id);       // Kiểm tra user còn hoạt động

    // ==============================
    // Lấy danh sách User
    // ==============================
    List<User> findByDeletedAtIsNull();                    // Lấy tất cả user chưa bị xóa

    // ==============================
    // Lấy chi tiết User
    // ==============================
    Optional<User> findByIdAndDeletedAtIsNull(String id);  // Lấy user chưa bị xóa theo id
    Optional<User> findByEmail(String email);              // Lấy user theo email (bao gồm đã xóa)
    Optional<User> findByEmailAndDeletedAtIsNull(String email); // Lấy user chưa bị xóa theo email
}
