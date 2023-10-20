package xrzqx.authresfulapi.repository;

import org.springframework.data.repository.CrudRepository;
import xrzqx.authresfulapi.entity.UserRedisRefreshToken;

import java.util.Optional;

public interface UserRedisRefreshTokenRepository extends CrudRepository<UserRedisRefreshToken, String> {
    // You can add custom queries here if needed
    Optional<UserRedisRefreshToken> findFirstByAccessToken(String token);
}
