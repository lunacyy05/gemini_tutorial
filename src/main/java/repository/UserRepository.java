package repository;

import entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u WHERE u.phoneNumber = :phoneNumber")
    Optional<User> findByPhoneNumber(@Param("phoneNumber") String phoneNumber);

    @Query("SELECT u FROM User u WHERE u.age BETWEEN :minAge AND :maxAge")
    Iterable<User> findByAgeBetween(@Param("minAge") Integer minAge, @Param("maxAge") Integer maxAge);
}