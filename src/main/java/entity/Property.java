package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "properties")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 200)
    private String address;

    @Column(nullable = false)
    private String district;

    @Column(nullable = false)
    private String neighborhood;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PropertyType propertyType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType roomType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RentalType rentalType;

    @Column(nullable = false)
    private Integer deposit;

    @Column(nullable = false)
    private Integer monthlyRent;

    @Column(nullable = false)
    private Integer maintenanceFee;

    @Column(nullable = false)
    private Double area;

    @Column(nullable = false)
    private Integer floor;

    @Column(nullable = false)
    private Integer totalFloor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus availabilityStatus;

    private LocalDateTime availableDate;

    @Column(nullable = false)
    private Boolean isParkingAvailable;

    @Column(nullable = false)
    private Boolean isElevatorAvailable;

    @Column(length = 50)
    private String contactName;

    @Column(length = 15)
    private String contactPhone;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<PropertyImage> images;

    @OneToMany(mappedBy = "property", cascade = CascadeType.ALL)
    private List<Bookmark> bookmarks;

    public enum PropertyType {
        APARTMENT, VILLA, ONE_ROOM, TWO_ROOM, OFFICE_TEL, STUDIO
    }

    public enum RoomType {
        ONE_ROOM, TWO_ROOM, THREE_ROOM, FOUR_ROOM_PLUS
    }

    public enum RentalType {
        JEONSE, MONTHLY_RENT, MIXED
    }

    public enum AvailabilityStatus {
        AVAILABLE, RENTED, PENDING
    }
}