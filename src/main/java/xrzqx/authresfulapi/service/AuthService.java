package xrzqx.authresfulapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.entity.UserRedisRefreshToken;
import xrzqx.authresfulapi.model.LoginUserRequest;
import xrzqx.authresfulapi.model.TokenResponse;
import xrzqx.authresfulapi.model.UserResponse;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRedisRefreshTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.security.BCrypt;

import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRedisRefreshTokenRepository userRedisRefreshTokenRepository;

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
            Optional<UserRedisRefreshToken> optionalUserRedisRefreshToken = userRedisRefreshTokenRepository.findById(user.getUsername());
            if (optionalUserRedisRefreshToken.isPresent()) {
                UserRedisRefreshToken userRedisRefreshToken = optionalUserRedisRefreshToken.get();
                UserRedisAccessToken userRedisAccessToken = getAccessToken(userRedisRefreshToken, user);
                return TokenResponse.builder()
                        .token(userRedisAccessToken.getAccessToken())
                        .expiredAt(userRedisAccessToken.getTtl())
                        .build();
            } else {
                UserRedisRefreshToken userRedisRefreshToken = new UserRedisRefreshToken();
                userRedisRefreshToken.setId(user.getUsername());
                userRedisRefreshToken.setRefreshToken(UUID.randomUUID().toString());
                userRedisRefreshToken.setTtl(604800L);

                UserRedisAccessToken userRedisAccessToken = getAccessToken(userRedisRefreshToken, user);

                userRedisRefreshToken.setAccessToken(userRedisAccessToken.getAccessToken());
                userRedisRefreshTokenRepository.save(userRedisRefreshToken);
                return TokenResponse.builder()
                        .token(userRedisAccessToken.getAccessToken())
                        .expiredAt(userRedisAccessToken.getTtl())
                        .build();
            }

        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or password wrong");
        }
    }

//    private Long next7Days() {
//        return System.currentTimeMillis() + (1000L * 60L * 60L * 24L * 7L);
//    }
//
//    private Long next1Minute() {
//        return System.currentTimeMillis() + (1000L * 60L);
//    }

    private UserRedisAccessToken getAccessToken(UserRedisRefreshToken userRedisRefreshToken, User user) {
        Optional<UserRedisAccessToken> optionalUserRedisAccessToken = userRedisAccessTokenRepository.findById(userRedisRefreshToken.getRefreshToken());
        if (optionalUserRedisAccessToken.isPresent()) {
            UserRedisAccessToken userRedisAccessToken = optionalUserRedisAccessToken.get();
            return userRedisAccessToken;
        } else {
            UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
            userRedisAccessToken.setId(userRedisRefreshToken.getRefreshToken());
            userRedisAccessToken.setAccessToken(UUID.randomUUID().toString());
            userRedisAccessToken.setUsername(user.getUsername());
            userRedisAccessToken.setName(user.getName());
            userRedisAccessToken.setEmail(user.getEmail());
            userRedisAccessToken.setTtl(60L);
            userRedisAccessTokenRepository.save(userRedisAccessToken);
            UserRedisRefreshToken newUserRedisRefreshToken = userRedisRefreshToken;
            newUserRedisRefreshToken.setAccessToken(userRedisAccessToken.getAccessToken());
            userRedisRefreshTokenRepository.save(newUserRedisRefreshToken);
            return userRedisAccessToken;
        }
    }

    @Transactional
    public void setAccessToken(UserRedisAccessToken userRedisAccessToken, UserRedisRefreshToken userRedisRefreshToken){
        userRedisAccessTokenRepository.save(userRedisAccessToken);
        userRedisRefreshTokenRepository.save(userRedisRefreshToken);
    }

    public TokenResponse get(UserRedisAccessToken user) {
        return TokenResponse.builder()
                .token(user.getAccessToken())
                .expiredAt(user.getTtl())
                .build();
    }

    @Transactional
    public void logout(UserRedisAccessToken user) {
        userRedisAccessTokenRepository.delete(user);
    }
}
