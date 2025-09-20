package dto;

import entity.Property;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class PropertyDto {

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class CreateRequest {
        private String title;
        private String description;
        private String address;
        private String district;
        private String neighborhood;
        private Property.PropertyType propertyType;
        private Property.RoomType roomType;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Property.RentalType rentalType;
        private Integer deposit;
        private Integer monthlyRent;
        private Integer maintenanceFee;
        private Double area;
        private Integer floor;
        private Integer totalFloor;
        private LocalDateTime availableDate;
        private Boolean isParkingAvailable;
        private Boolean isElevatorAvailable;
        private String contactName;
        private String contactPhone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class UpdateRequest {
        private String title;
        private String description;
        private Property.RentalType rentalType;
        private Integer deposit;
        private Integer monthlyRent;
        private Integer maintenanceFee;
        private LocalDateTime availableDate;
        private Property.AvailabilityStatus availabilityStatus;
        private String contactName;
        private String contactPhone;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Response {
        private Long id;
        private String title;
        private String description;
        private String address;
        private String district;
        private String neighborhood;
        private Property.PropertyType propertyType;
        private Property.RoomType roomType;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Property.RentalType rentalType;
        private Integer deposit;
        private Integer monthlyRent;
        private Integer maintenanceFee;
        private Double area;
        private Integer floor;
        private Integer totalFloor;
        private Property.AvailabilityStatus availabilityStatus;
        private LocalDateTime availableDate;
        private Boolean isParkingAvailable;
        private Boolean isElevatorAvailable;
        private String contactName;
        private String contactPhone;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private List<PropertyImageDto.Response> images;

        public static Response fromEntity(Property property) {
            return Response.builder()
                    .id(property.getId())
                    .title(property.getTitle())
                    .description(property.getDescription())
                    .address(property.getAddress())
                    .district(property.getDistrict())
                    .neighborhood(property.getNeighborhood())
                    .propertyType(property.getPropertyType())
                    .roomType(property.getRoomType())
                    .latitude(property.getLatitude())
                    .longitude(property.getLongitude())
                    .rentalType(property.getRentalType())
                    .deposit(property.getDeposit())
                    .monthlyRent(property.getMonthlyRent())
                    .maintenanceFee(property.getMaintenanceFee())
                    .area(property.getArea())
                    .floor(property.getFloor())
                    .totalFloor(property.getTotalFloor())
                    .availabilityStatus(property.getAvailabilityStatus())
                    .availableDate(property.getAvailableDate())
                    .isParkingAvailable(property.getIsParkingAvailable())
                    .isElevatorAvailable(property.getIsElevatorAvailable())
                    .contactName(property.getContactName())
                    .contactPhone(property.getContactPhone())
                    .createdAt(property.getCreatedAt())
                    .updatedAt(property.getUpdatedAt())
                    .build();
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class SearchRequest {
        private String location;
        private Integer minBudget;
        private Integer maxBudget;
        private Property.RentalType rentalType;
        private Property.PropertyType propertyType;
        private Property.RoomType roomType;
        private BigDecimal latitude;
        private BigDecimal longitude;
        private Double radius;
        private Integer page;
        private Integer size;
        private String sortBy;
        private String sortDirection;
    }
}