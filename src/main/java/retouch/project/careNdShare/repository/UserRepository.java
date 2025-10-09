package retouch.project.careNdShare.repository;


import org.springframework.data.jpa.repository.Query;
import retouch.project.careNdShare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Optional<User> findByResetToken(String resetToken);
    Boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u WHERE u.isAdmin = true")
    long countByAdminTrue();
}