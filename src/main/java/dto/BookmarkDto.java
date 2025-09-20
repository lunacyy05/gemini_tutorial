package dto;

import entity.Bookmark;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class BookmarkDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long propertyId;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private PropertyDto.Response property;
        private LocalDateTime createdAt;

        public static Response fromEntity(Bookmark bookmark) {
            return Response.builder()
                    .id(bookmark.getId())
                    .property(PropertyDto.Response.fromEntity(bookmark.getProperty()))
                    .createdAt(bookmark.getCreatedAt())
                    .build();
        }
    }
}