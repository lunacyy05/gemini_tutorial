package repository;

import entity.Bookmark;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId ORDER BY b.createdAt DESC")
    Page<Bookmark> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT b FROM Bookmark b WHERE b.user.id = :userId AND b.property.id = :propertyId")
    Optional<Bookmark> findByUserIdAndPropertyId(@Param("userId") Long userId,
                                                  @Param("propertyId") Long propertyId);

    boolean existsByUserIdAndPropertyId(Long userId, Long propertyId);

    @Query("SELECT COUNT(b) FROM Bookmark b WHERE b.property.id = :propertyId")
    Long countByPropertyId(@Param("propertyId") Long propertyId);

    void deleteByUserIdAndPropertyId(Long userId, Long propertyId);
}