package xrzqx.authresfulapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;

@Repository
public interface UserRedisAccessTokenRepository extends CrudRepository<UserRedisAccessToken, String> {
    // You can add custom queries here if needed
}
