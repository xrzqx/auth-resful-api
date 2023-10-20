package xrzqx.authresfulapi.resolver;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.entity.UserRedisRefreshToken;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRedisRefreshTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.service.AuthService;

import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRedisAccessTokenRepository userRedisAccessTokenRepository;

    @Autowired
    private UserRedisRefreshTokenRepository userRedisRefreshTokenRepository;

    @Autowired
    private AuthService authService;
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return UserRedisAccessToken.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String token = servletRequest.getHeader("X-API-TOKEN");
        log.info("TOKEN {}",token);
        if (token == null){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }
        UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
        Optional<UserRedisAccessToken> optionalUserRedisAccessToken = userRedisAccessTokenRepository.findById(token);
        if (optionalUserRedisAccessToken.isPresent()){
            userRedisAccessToken = optionalUserRedisAccessToken.get();
        }
        else{
            String acst = String.valueOf(userRedisRefreshTokenRepository.findFirstByAccessToken(token));
            log.info("ACST {}",acst);
            UserRedisRefreshToken userRedisRefreshToken = userRedisRefreshTokenRepository.findFirstByAccessToken(token)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            log.info("USER_RR {}",userRedisRefreshToken);
            User user = userRepository.findById(userRedisRefreshToken.getId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));
            log.info("USER {}",user);
            userRedisAccessToken.setId(userRedisRefreshToken.getRefreshToken());
            userRedisAccessToken.setAccessToken(UUID.randomUUID().toString());
            userRedisAccessToken.setTtl(60L);
            userRedisAccessToken.setUsername(user.getUsername());
            userRedisAccessToken.setName(user.getName());
            userRedisAccessToken.setEmail(user.getEmail());
            userRedisRefreshToken.setAccessToken(userRedisAccessToken.getAccessToken());
            authService.setAccessToken(userRedisAccessToken, userRedisRefreshToken);
        }

        return userRedisAccessToken;
    }
}
