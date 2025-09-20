package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "search_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(length = 100)
    private String location;

    private Integer minBudget;

    private Integer maxBudget;

    @Enumerated(EnumType.STRING)
    private Property.RentalType rentalType;

    @Enumerated(EnumType.STRING)
    private Property.PropertyType propertyType;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime searchedAt;
}