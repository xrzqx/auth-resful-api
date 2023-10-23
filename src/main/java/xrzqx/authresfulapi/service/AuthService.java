package xrzqx.authresfulapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.model.AccessTokenRequest;
import xrzqx.authresfulapi.model.AccessTokenResponse;
import xrzqx.authresfulapi.model.LoginUserRequest;
import xrzqx.authresfulapi.model.TokenResponse;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.security.BCrypt;

import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRedisAccessTokenRepository userRedisAccessTokenRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public TokenResponse login(LoginUserRequest request) {
        validationService.validate(request);

        User user = userRepository.findById(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username not found"));

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            if (user.getRefreshToken() != null){
                if (user.getRefreshTokenExpiredAt() < System.currentTimeMillis()
                        || user.getRefreshTokenExpiredAt() == null){
                    UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
                    setNewRefreshToken(user, userRedisAccessToken);
                    userRepository.save(user);
                    userRedisAccessTokenRepository.save(userRedisAccessToken);
                }
                else {
                    if (user.getAccessTokenExpiredAt() < System.currentTimeMillis()
                            || user.getAccessTokenExpiredAt() == null){
                        UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
                        user.setAccessToken(UUID.randomUUID().toString());
                        user.setAccessTokenExpiredAt(next1Minute());
                        setCacheUser(user,userRedisAccessToken);
                        userRepository.save(user);
                        userRedisAccessTokenRepository.save(userRedisAccessToken);
                    }
                }
            }
            else {
                UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
                setNewRefreshToken(user, userRedisAccessToken);
                userRepository.save(user);
                userRedisAccessTokenRepository.save(userRedisAccessToken);
            }

            return TokenResponse.builder()
                    .refreshToken(user.getRefreshToken())
                    .accessToken(user.getAccessToken())
                    .expiredAt(user.getAccessTokenExpiredAt())
                    .build();

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong");
        }
    }

    private Long next7Days() {
        return System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 7L);
    }

    private Long next1Minute() {
        return System.currentTimeMillis() + (1000L * 60L);
    }

    private void setNewRefreshToken(User user, UserRedisAccessToken userRedisAccessToken){
        user.setRefreshToken(UUID.randomUUID().toString());
        user.setRefreshTokenExpiredAt(next7Days());

        user.setAccessToken(UUID.randomUUID().toString());
        user.setAccessTokenExpiredAt(next1Minute());

        setCacheUser(user, userRedisAccessToken);

    }

    private void  setCacheUser(User user, UserRedisAccessToken userRedisAccessToken) {
        userRedisAccessToken.setId(user.getAccessToken());
        userRedisAccessToken.setExpireAt(user.getAccessTokenExpiredAt());
        userRedisAccessToken.setUsername(user.getUsername());
        userRedisAccessToken.setName(user.getName());
        userRedisAccessToken.setEmail(user.getEmail());
        userRedisAccessToken.setTtl(60L);
    }

    @Transactional
    public AccessTokenResponse token(AccessTokenRequest request){
        validationService.validate(request);

        User user = userRepository.findFirstByRefreshToken(request.getToken())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        if (user.getRefreshTokenExpiredAt() < System.currentTimeMillis()){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
        user.setAccessToken(UUID.randomUUID().toString());
        user.setAccessTokenExpiredAt(next1Minute());
        setCacheUser(user,userRedisAccessToken);
        userRepository.save(user);
        userRedisAccessTokenRepository.save(userRedisAccessToken);

        return AccessTokenResponse.builder()
                .accessToken(user.getAccessToken())
                .expiredAt(user.getAccessTokenExpiredAt())
                .build();
    }

    @Transactional
    public void logout(UserRedisAccessToken user) {
        userRedisAccessTokenRepository.delete(user);
    }
}
