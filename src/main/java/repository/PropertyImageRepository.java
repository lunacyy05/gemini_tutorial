package repository;

import entity.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, Long> {

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId ORDER BY pi.imageOrder ASC")
    List<PropertyImage> findByPropertyIdOrderByImageOrder(@Param("propertyId") Long propertyId);

    @Query("SELECT pi FROM PropertyImage pi WHERE pi.property.id = :propertyId AND pi.imageOrder = 1")
    PropertyImage findMainImageByPropertyId(@Param("propertyId") Long propertyId);

    void deleteByPropertyId(Long propertyId);
}