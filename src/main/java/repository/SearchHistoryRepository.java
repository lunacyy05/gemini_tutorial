package repository;

import entity.SearchHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user.id = :userId ORDER BY sh.searchedAt DESC")
    Page<SearchHistory> findByUserIdOrderBySearchedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user.id = :userId AND sh.searchedAt >= :fromDate ORDER BY sh.searchedAt DESC")
    List<SearchHistory> findRecentSearchesByUserId(@Param("userId") Long userId,
                                                   @Param("fromDate") LocalDateTime fromDate);

    @Query("SELECT sh.location, COUNT(sh) as searchCount FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "GROUP BY sh.location " +
           "ORDER BY searchCount DESC")
    List<Object[]> findMostSearchedLocationsByUserId(@Param("userId") Long userId);

    void deleteByUserId(Long userId);
}