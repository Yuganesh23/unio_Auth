package repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Repository;
import entity.SignUpEntity;

@Repository
public interface SignUpRepository extends JpaRepository<SignUpEntity, Long> {
    Optional<SignUpEntity> findByEmail(String email);
   // ResponseEntity<String> verifyOtp(String email, String userOtp, String password);

}
