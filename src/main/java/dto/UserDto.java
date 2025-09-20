package dto;

import entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class UserDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String email;
        private String password;
        private String name;
        private String phoneNumber;
        private Integer age;
        private User.Gender gender;
        private Integer maxBudget;
        private String preferredLocation;
        private User.RoomType preferredRoomType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String name;
        private String phoneNumber;
        private Integer age;
        private Integer maxBudget;
        private String preferredLocation;
        private User.RoomType preferredRoomType;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String email;
        private String name;
        private String phoneNumber;
        private Integer age;
        private User.Gender gender;
        private Integer maxBudget;
        private String preferredLocation;
        private User.RoomType preferredRoomType;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response fromEntity(User user) {
            return Response.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .phoneNumber(user.getPhoneNumber())
                    .age(user.getAge())
                    .gender(user.getGender())
                    .maxBudget(user.getMaxBudget())
                    .preferredLocation(user.getPreferredLocation())
                    .preferredRoomType(user.getPreferredRoomType())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .build();
        }
    }
}