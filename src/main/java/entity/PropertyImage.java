package entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "property_images")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PropertyImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    private Property property;

    @Column(nullable = false, length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Integer imageOrder;

    @Column(length = 100)
    private String altText;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}