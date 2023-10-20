package xrzqx.authresfulapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.model.RegisterUserRequest;
import xrzqx.authresfulapi.model.UpdateUserRequest;
import xrzqx.authresfulapi.model.UserResponse;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.security.BCrypt;

import java.util.Objects;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRedisAccessTokenRepository userRedisAccessTokenRepository;

    @Autowired
    private ValidationService validationService;

    @Transactional
    public void register(RegisterUserRequest request) {

        validationService.validate(request);

        if (userRepository.existsById(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        userRepository.save(user);

    }

    public UserResponse get(UserRedisAccessToken user) {
        return UserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }

    @Transactional
    public UserResponse update(UpdateUserRequest request, UserRedisAccessToken userRedisAccessToken) {
        validationService.validate(request);
        User user = userRepository.findById(userRedisAccessToken.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username not found"));
        if (Objects.nonNull(request.getName())) {
            user.setName(request.getName());
            userRedisAccessToken.setName(user.getName());
        }
        if (Objects.nonNull(request.getPassword())) {
            user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
            userRedisAccessTokenRepository.delete(userRedisAccessToken);
        }
        userRepository.save(user);
        return UserResponse.builder()
                .username(user.getUsername())
                .name(user.getName())
                .build();
    }


}
