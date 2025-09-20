package repository;

import entity.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByAvailabilityStatus(Property.AvailabilityStatus status, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.district = :district AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByDistrictAndAvailable(@Param("district") String district, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.neighborhood = :neighborhood AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByNeighborhoodAndAvailable(@Param("neighborhood") String neighborhood, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.deposit BETWEEN :minDeposit AND :maxDeposit AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByDepositRange(@Param("minDeposit") Integer minDeposit,
                                     @Param("maxDeposit") Integer maxDeposit,
                                     Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.monthlyRent BETWEEN :minRent AND :maxRent AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByMonthlyRentRange(@Param("minRent") Integer minRent,
                                         @Param("maxRent") Integer maxRent,
                                         Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.rentalType = :rentalType AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByRentalTypeAndAvailable(@Param("rentalType") Property.RentalType rentalType,
                                               Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.propertyType = :propertyType AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByPropertyTypeAndAvailable(@Param("propertyType") Property.PropertyType propertyType,
                                                 Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.roomType = :roomType AND p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findByRoomTypeAndAvailable(@Param("roomType") Property.RoomType roomType,
                                             Pageable pageable);

    @Query(value = "SELECT * FROM properties p WHERE " +
           "ST_Distance_Sphere(POINT(p.longitude, p.latitude), POINT(:longitude, :latitude)) <= :radius " +
           "AND p.availability_status = 'AVAILABLE' " +
           "ORDER BY ST_Distance_Sphere(POINT(p.longitude, p.latitude), POINT(:longitude, :latitude))",
           nativeQuery = true)
    List<Property> findNearbyProperties(@Param("latitude") BigDecimal latitude,
                                       @Param("longitude") BigDecimal longitude,
                                       @Param("radius") Double radius);

    @Query("SELECT p FROM Property p WHERE " +
           "(:district IS NULL OR p.district = :district) AND " +
           "(:neighborhood IS NULL OR p.neighborhood = :neighborhood) AND " +
           "(:minDeposit IS NULL OR p.deposit >= :minDeposit) AND " +
           "(:maxDeposit IS NULL OR p.deposit <= :maxDeposit) AND " +
           "(:minRent IS NULL OR p.monthlyRent >= :minRent) AND " +
           "(:maxRent IS NULL OR p.monthlyRent <= :maxRent) AND " +
           "(:rentalType IS NULL OR p.rentalType = :rentalType) AND " +
           "(:propertyType IS NULL OR p.propertyType = :propertyType) AND " +
           "(:roomType IS NULL OR p.roomType = :roomType) AND " +
           "p.availabilityStatus = 'AVAILABLE'")
    Page<Property> findPropertiesWithFilters(@Param("district") String district,
                                            @Param("neighborhood") String neighborhood,
                                            @Param("minDeposit") Integer minDeposit,
                                            @Param("maxDeposit") Integer maxDeposit,
                                            @Param("minRent") Integer minRent,
                                            @Param("maxRent") Integer maxRent,
                                            @Param("rentalType") Property.RentalType rentalType,
                                            @Param("propertyType") Property.PropertyType propertyType,
                                            @Param("roomType") Property.RoomType roomType,
                                            Pageable pageable);
}