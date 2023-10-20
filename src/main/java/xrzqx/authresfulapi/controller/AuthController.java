package xrzqx.authresfulapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.entity.UserRedisRefreshToken;
import xrzqx.authresfulapi.model.LoginUserRequest;
import xrzqx.authresfulapi.model.TokenResponse;
import xrzqx.authresfulapi.model.WebResponse;
import xrzqx.authresfulapi.service.AuthService;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(
            path = "/api/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TokenResponse> login(@RequestBody LoginUserRequest request){
        TokenResponse tokenResponse = authService.login(request);
        return WebResponse.<TokenResponse>builder().data(tokenResponse).build();
    }

    @GetMapping(
            path = "/api/auth/token",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<TokenResponse> get(UserRedisAccessToken user){
        TokenResponse tokenResponse = authService.get(user);
        return WebResponse.<TokenResponse>builder().data(tokenResponse).build();
    }

    @DeleteMapping(
            path = "/api/auth/logout",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public WebResponse<String> logout(UserRedisAccessToken user){
        authService.logout(user);
        return WebResponse.<String>builder().data("OK").build();
    }
}
