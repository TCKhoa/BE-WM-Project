package org.wp.wpproject.repository;

import org.wp.wpproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);


    List<User> findByDeletedAtIsNull();

    Optional<User> findByIdAndDeletedAtIsNull(String id);
    Optional<User> findByEmail(String email);
    boolean existsByIdAndDeletedAtIsNull(String id);
    Optional<User> findByEmailAndDeletedAtIsNull(String email);

}
