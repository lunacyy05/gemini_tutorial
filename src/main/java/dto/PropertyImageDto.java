package dto;

import entity.PropertyImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class PropertyImageDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private Long propertyId;
        private String imageUrl;
        private Integer imageOrder;
        private String altText;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String imageUrl;
        private Integer imageOrder;
        private String altText;
        private LocalDateTime createdAt;

        public static Response fromEntity(PropertyImage image) {
            return Response.builder()
                    .id(image.getId())
                    .imageUrl(image.getImageUrl())
                    .imageOrder(image.getImageOrder())
                    .altText(image.getAltText())
                    .createdAt(image.getCreatedAt())
                    .build();
        }
    }
}