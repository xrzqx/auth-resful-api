package xrzqx.authresfulapi.repository;

import org.springframework.data.repository.CrudRepository;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;

public interface UserRedisAccessTokenRepository extends CrudRepository<UserRedisAccessToken, String> {
    // You can add custom queries here if needed
}
