package com.echo_english.service;

import com.echo_english.dto.request.UpdateUserRequestDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.echo_english.entity.User;
import com.echo_english.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    public boolean isEmailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean create(User user) {
        try {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void activateUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setActive(true);
        userRepository.save(user);
    }

    public void updatePassword(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional // Đảm bảo toàn vẹn dữ liệu cho cả quá trình cập nhật
    public User updateUser(Long userId, UpdateUserRequestDto request) {
        // 1. Tìm user theo ID
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User with ID " + userId + " not found")); // Sử dụng RuntimeException hoặc custom exception

        // 2. Cập nhật các trường (chỉ khi request có giá trị)
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }

        if (request.getAvatar() != null) { // Cho phép set avatar thành null hoặc rỗng
            user.setAvatar(request.getAvatar().trim());
        }

        // Cập nhật email và kiểm tra trùng lặp (trừ email của chính user đó)
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            String newEmail = request.getEmail().trim();
            if (!user.getEmail().equalsIgnoreCase(newEmail)) { // Chỉ kiểm tra nếu email mới khác email cũ
                if (isEmailExists(newEmail)) {
                    throw new IllegalArgumentException("Email address already exists."); // Throw lỗi nếu email đã tồn tại
                }
                user.setEmail(newEmail);
                // Nếu email thay đổi, có thể cân nhắc yêu cầu xác minh lại email (optional, tùy logic ứng dụng)
                // user.setActive(false); // Ví dụ: set active về false
            }
        }

        // 3. Cập nhật mật khẩu (chỉ khi request có giá trị và gọi phương thức hiện có)
        boolean passwordUpdated = false;
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            // Gọi phương thức updatePassword hiện có.
            // Lưu ý: Phương thức này cũng save userRepository.save(user);
            // Nếu các trường khác cũng được cập nhật, cần cân nhắc lại logic save.
            // Trong trường hợp này, updatePassword sẽ save, nên ta chỉ cần save user
            // nếu KHÔNG CÓ password được update.
            try {
                this.updatePassword(user.getEmail(), request.getPassword()); // Gọi phương thức updatePassword đã có
                passwordUpdated = true;
            } catch (IllegalArgumentException e) {
                // Lỗi này xảy ra nếu findByEmail trong updatePassword không tìm thấy user,
                // nhưng ở đây chúng ta đã tìm thấy user theo ID, nên lỗi này khó xảy ra
                // trừ khi có vấn đề đồng bộ hoặc logic sai.
                throw new RuntimeException("Error updating password for found user.", e);
            }
        }

        // 4. Lưu User nếu không có mật khẩu được cập nhật (vì updatePassword đã save)
        if (!passwordUpdated) {
            userRepository.save(user);
        }

        // 5. Trả về user đã cập nhật
        return user;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with ID " + id + " not found"));
    }
}
