package controller;

import dto.UserDto;
import entity.User;
import repository.UserRepository;
import security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmail(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = tokenProvider.generateToken(authentication);

            Optional<User> userOptional = userRepository.findByEmail(loginRequest.getEmail());
            if (userOptional.isEmpty()) {
                return ResponseEntity.badRequest().body("사용자를 찾을 수 없습니다.");
            }

            User user = userOptional.get();
            UserDto.Response userResponse = UserDto.Response.fromEntity(user);

            Map<String, Object> response = new HashMap<>();
            response.put("token", jwt);
            response.put("type", "Bearer");
            response.put("user", userResponse);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("이메일 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody SignUpRequest signUpRequest) {
        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity.badRequest()
                    .body("이미 사용 중인 이메일입니다.");
        }

        User user = User.builder()
                .name(signUpRequest.getName())
                .email(signUpRequest.getEmail())
                .password(passwordEncoder.encode(signUpRequest.getPassword()))
                .phoneNumber(signUpRequest.getPhoneNumber())
                .build();

        User savedUser = userRepository.save(user);
        String jwt = tokenProvider.generateTokenFromUsername(savedUser.getEmail());

        UserDto.Response userResponse = UserDto.Response.fromEntity(savedUser);

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwt);
        response.put("type", "Bearer");
        response.put("user", userResponse);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        SecurityContextHolder.clearContext();

        Map<String, String> response = new HashMap<>();
        response.put("message", "로그아웃이 완료되었습니다.");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestHeader("Authorization") String token) {
        try {
            if (token.startsWith("Bearer ")) {
                token = token.substring(7);
            }

            if (tokenProvider.validateToken(token)) {
                String username = tokenProvider.getUsernameFromToken(token);
                String newToken = tokenProvider.generateTokenFromUsername(username);

                Map<String, Object> response = new HashMap<>();
                response.put("token", newToken);
                response.put("type", "Bearer");

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("유효하지 않은 토큰입니다.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("토큰 갱신에 실패했습니다.");
        }
    }

    public static class LoginRequest {
        private String email;
        private String password;

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class SignUpRequest {
        private String name;
        private String email;
        private String password;
        private String phoneNumber;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    }
}