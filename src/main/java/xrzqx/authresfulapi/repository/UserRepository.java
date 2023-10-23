package xrzqx.authresfulapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import xrzqx.authresfulapi.entity.User;

import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // You can add custom query methods here if needed
    Optional<User> findFirstByRefreshToken(String token);
}
