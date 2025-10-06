package retouch.project.careNdShare.controller;


import retouch.project.careNdShare.dto.*;
import retouch.project.careNdShare.entity.User;
import retouch.project.careNdShare.service.AuthService;
import retouch.project.careNdShare.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest,
                                              HttpServletResponse response) {
        try {
            String jwt = authService.authenticateUser(loginRequest.getEmail(), loginRequest.getPassword());
            User user = userService.findByEmail(loginRequest.getEmail())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create HTTP-only cookie
            Cookie jwtCookie = new Cookie("jwtToken", jwt);
            jwtCookie.setHttpOnly(true);
            jwtCookie.setSecure(false); // Set to true in production with HTTPS
            jwtCookie.setPath("/");
            jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours
            response.addCookie(jwtCookie);

            JwtResponse jwtResponse = new JwtResponse(jwt, user.getEmail(),
                    user.getFirstName(), user.getLastName());

            return ResponseEntity.ok(jwtResponse);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest) {
        try {
            User user = userService.registerUser(
                    registerRequest.getEmail(),
                    registerRequest.getPassword(),
                    registerRequest.getFirstName(),
                    registerRequest.getLastName());

            // Return a proper JSON response
            Map<String, String> response = new HashMap<>();
            response.put("message", "User registered successfully!");
            response.put("email", user.getEmail());
            response.put("firstName", user.getFirstName());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // Return proper error response
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        // Clear the JWT cookie
        Cookie jwtCookie = new Cookie("jwtToken", null);
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false);
        jwtCookie.setPath("/");
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);

        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String email = request.getEmail();

            // Check if user exists
            Optional<User> userOptional = userService.findByEmail(email);

            if (userOptional.isPresent()) {
                // Create and send reset token
                userService.createPasswordResetToken(email);
            }

            // Always return success message for security (don't reveal if email exists)
            String message = "If this email exists in our system, a password reset link will be sent.";

            return ResponseEntity.ok(new ForgotPasswordResponse(message, true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ForgotPasswordResponse("Error processing request. Please try again.", false));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            // Validate passwords match
            if (!request.getNewPassword().equals(request.getConfirmPassword())) {
                return ResponseEntity.badRequest()
                        .body(new ForgotPasswordResponse("Passwords do not match", false));
            }

            // Validate token
            if (!userService.validateResetToken(request.getToken())) {
                return ResponseEntity.badRequest()
                        .body(new ForgotPasswordResponse("Invalid or expired reset token", false));
            }

            // Reset password
            userService.resetPassword(request.getToken(), request.getNewPassword());

            return ResponseEntity.ok(new ForgotPasswordResponse("Password reset successfully", true));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ForgotPasswordResponse("Error resetting password: " + e.getMessage(), false));
        }
    }

    @GetMapping("/validate-reset-token")
    public ResponseEntity<?> validateResetToken(@RequestParam String token) {
        try {
            boolean isValid = userService.validateResetToken(token);
            if (isValid) {
                return ResponseEntity.ok(new ForgotPasswordResponse("Token is valid", true));
            } else {
                return ResponseEntity.badRequest()
                        .body(new ForgotPasswordResponse("Invalid or expired token", false));
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ForgotPasswordResponse("Error validating token", false));
        }
    }

}