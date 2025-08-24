package org.wp.wpproject.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.wp.wpproject.entity.User;
import org.wp.wpproject.repository.UserRepository;

import java.util.Collections;

@Service
@Primary // Đánh dấu ưu tiên nếu có nhiều bean UserDetailsService
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    /**
     * Spring Security sẽ gọi hàm này khi cần load user để xác thực.
     * Ở đây "username" chính là email.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndDeletedAtIsNull(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Không tìm thấy người dùng với email: " + email));

        // Tạo authority từ role (ví dụ: ADMIN → ROLE_ADMIN)
        GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());

        // Trả về UserDetails cho Spring Security
        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail()) // dùng email làm username
                .password(user.getPassword()) // password đã BCrypt encode
                .authorities(Collections.singleton(authority)) // danh sách quyền
                .build();
    }
}
