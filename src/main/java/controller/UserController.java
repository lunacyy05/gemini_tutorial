package controller;

import dto.UserDto;
import entity.User;
import repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<UserDto.Response> createUser(@RequestBody UserDto.CreateRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(request.getPassword())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .age(request.getAge())
                .gender(request.getGender())
                .maxBudget(request.getMaxBudget())
                .preferredLocation(request.getPreferredLocation())
                .preferredRoomType(request.getPreferredRoomType())
                .build();

        User savedUser = userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UserDto.Response.fromEntity(savedUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto.Response> getUserById(@PathVariable Long id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> ResponseEntity.ok(UserDto.Response.fromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<UserDto.Response>> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto.Response> responses = users.stream()
                .map(UserDto.Response::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto.Response> getUserByEmail(@PathVariable String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> ResponseEntity.ok(UserDto.Response.fromEntity(u)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto.Response> updateUser(@PathVariable Long id,
                                                      @RequestBody UserDto.UpdateRequest request) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        User user = optionalUser.get();
        if (request.getName() != null) user.setName(request.getName());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        if (request.getAge() != null) user.setAge(request.getAge());
        if (request.getMaxBudget() != null) user.setMaxBudget(request.getMaxBudget());
        if (request.getPreferredLocation() != null) user.setPreferredLocation(request.getPreferredLocation());
        if (request.getPreferredRoomType() != null) user.setPreferredRoomType(request.getPreferredRoomType());

        User updatedUser = userRepository.save(user);
        return ResponseEntity.ok(UserDto.Response.fromEntity(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}