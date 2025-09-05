package org.wp.wpproject.repository;

import org.wp.wpproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    // ==============================
    // Kiểm tra sự tồn tại (chỉ với user chưa bị xóa)
    // ==============================
    boolean existsByStaffCodeAndDeletedAtIsNull(String staffCode); // Kiểm tra staffCode chưa bị xóa
    boolean existsByUsernameAndDeletedAtIsNull(String username);   // Kiểm tra username chưa bị xóa
    boolean existsByEmailAndDeletedAtIsNull(String email);         // Kiểm tra email chưa bị xóa
    boolean existsByPhoneAndDeletedAtIsNull(String phone);         // Kiểm tra số điện thoại chưa bị xóa

    boolean existsByIdAndDeletedAtIsNull(String id);               // Kiểm tra user còn hoạt động

    // ==============================
    // Lấy danh sách User
    // ==============================
    List<User> findByDeletedAtIsNull();                            // Lấy tất cả user chưa bị xóa

    // ==============================
    // Lấy chi tiết User
    // ==============================
    Optional<User> findByIdAndDeletedAtIsNull(String id);          // Lấy user chưa bị xóa theo id
    Optional<User> findByEmail(String email);                      // Lấy user theo email (kể cả đã xóa)
    Optional<User> findByEmailAndDeletedAtIsNull(String email);    // Lấy user chưa bị xóa theo email
}
