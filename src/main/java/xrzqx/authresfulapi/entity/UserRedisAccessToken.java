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
//@RedisHash(value = "users_access", timeToLive = 900)
@RedisHash(value = "users_access")
public class UserRedisAccessToken {

    //this will refer to access token
    @Id
    private String id;

    private String username;

    private String name;

    private String email;

    private long expireAt;

    @TimeToLive
    private long ttl;


}
