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
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
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
        UserRedisAccessToken userRedisAccessToken = userRedisAccessTokenRepository.findById(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized"));

        return userRedisAccessToken;
    }
}
