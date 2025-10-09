package retouch.project.careNdShare.service;

import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Value("${app.password-reset.expiration-minutes}")
    private int resetTokenExpirationMinutes;

    @Autowired
    private EmailService emailService;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUserRole(Long userId, boolean isAdmin) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setAdmin(isAdmin);
        return userRepository.save(user);
    }

    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(userId);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    public long getTotalUsers() {
        return userRepository.count();
    }

    public long getAdminUsersCount() {
        return userRepository.countByAdminTrue();
    }



    public User registerUser(String email, String password, String firstName, String lastName) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("Email is required");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new RuntimeException("Password is required");
        }
        if (firstName == null || firstName.trim().isEmpty()) {
            throw new RuntimeException("First name is required");
        }
        if (lastName == null || lastName.trim().isEmpty()) {
            throw new RuntimeException("Last name is required");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email is already registered!");
        }

        try {
            User user = new User();
            user.setEmail(email.trim().toLowerCase());
            user.setPassword(passwordEncoder.encode(password));
            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setRoles(Arrays.asList("ROLE_USER"));

            return userRepository.save(user);
        } catch (Exception e) {
            throw new RuntimeException("Failed to register user: " + e.getMessage());
        }
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByResetToken(String resetToken) {
        return userRepository.findByResetToken(resetToken);
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    public void createPasswordResetToken(String email) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            // Generate reset token
            String resetToken = UUID.randomUUID().toString();
            user.setResetToken(resetToken);
            user.setResetTokenExpiry(LocalDateTime.now().plusMinutes(resetTokenExpirationMinutes));

            userRepository.save(user);

            // Send email
            emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFirstName());
        }
    }

    public boolean validateResetToken(String resetToken) {
        Optional<User> userOptional = userRepository.findByResetToken(resetToken);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            return user.getResetTokenExpiry() != null &&
                    user.getResetTokenExpiry().isAfter(LocalDateTime.now());
        }
        return false;
    }

    public void resetPassword(String resetToken, String newPassword) {
        Optional<User> userOptional = userRepository.findByResetToken(resetToken);
        if (userOptional.isPresent()) {
            User user = userOptional.get();

            if (user.getResetTokenExpiry() != null &&
                    user.getResetTokenExpiry().isAfter(LocalDateTime.now())) {

                // Update password
                user.setPassword(passwordEncoder.encode(newPassword));

                // Clear reset token
                user.setResetToken(null);
                user.setResetTokenExpiry(null);

                userRepository.save(user);
            } else {
                throw new RuntimeException("Reset token has expired");
            }
        } else {
            throw new RuntimeException("Invalid reset token");
        }
    }
}
