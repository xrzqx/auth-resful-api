package xrzqx.authresfulapi.entity;

import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
//@RedisHash(value = "users_refresh", timeToLive = 604800)
@RedisHash(value = "users_refresh")
public class UserRedisRefreshToken {
    //this id will refer to username
    @Id
    private String id;

    private String refreshToken;

    private String accessToken;

    @TimeToLive
    private long ttl;

}
