package xrzqx.authresfulapi.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import xrzqx.authresfulapi.entity.User;
import xrzqx.authresfulapi.entity.UserRedisAccessToken;
import xrzqx.authresfulapi.model.*;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.security.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRedisAccessTokenRepository userRedisAccessTokenRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userRedisAccessTokenRepository.deleteAll();
    }

    @Test
    void loginFailedUserNotFound() throws Exception {
        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("test");
        request.setPassword("test");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void loginFailedWrongPassword() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("test");
        request.setPassword("salah");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void loginSuccess() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        userRepository.save(user);

        LoginUserRequest request = new LoginUserRequest();
        request.setUsername("test");
        request.setPassword("test");

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNull(response.getErrors());
            assertNotNull(response.getData().getRefreshToken());
            assertNotNull(response.getData().getAccessToken());
            assertNotNull(response.getData().getExpiredAt());

            User userDb = userRepository.findById("test").orElse(null);
            assertNotNull(userDb);
            assertEquals(userDb.getRefreshToken(), response.getData().getRefreshToken());
            assertEquals(userDb.getAccessToken(), response.getData().getAccessToken());
            assertEquals(userDb.getAccessTokenExpiredAt(), response.getData().getExpiredAt());
        });
    }

    @Test
    void tokenFailed() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        userRepository.save(user);

        AccessTokenRequest request = new AccessTokenRequest();
        request.setToken("salah");

        mockMvc.perform(
                post("/api/auth/token")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<AccessTokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void tokenSuccess() throws Exception {
        User user = new User();
        user.setName("Test");
        user.setUsername("test");
        user.setEmail("test@mail.com");
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        userRepository.save(user);

        AccessTokenRequest request = new AccessTokenRequest();
        request.setToken("refresh-test");

        mockMvc.perform(
                post("/api/auth/token")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AccessTokenResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNull(response.getErrors());
            assertNotNull(response.getData().getAccessToken());
            assertNotNull(response.getData().getExpiredAt());

            User userDb = userRepository.findById("test").orElse(null);
            assertNotNull(userDb);
            assertEquals(userDb.getAccessToken(), response.getData().getAccessToken());
            assertEquals(userDb.getAccessTokenExpiredAt(), response.getData().getExpiredAt());
        });
    }

    @Test
    void logoutFailed() throws Exception {
        mockMvc.perform(
                delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getErrors());
        });
    }

    @Test
    void logoutSuccess() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setName("Test");
        user.setEmail("test@mail.com");
        user.setPassword(BCrypt.hashpw("test", BCrypt.gensalt()));
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000L);
        userRepository.save(user);

        UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
        userRedisAccessToken.setId(user.getAccessToken());
        userRedisAccessToken.setUsername(user.getUsername());
        userRedisAccessToken.setName(user.getName());
        userRedisAccessToken.setEmail(user.getEmail());
        userRedisAccessToken.setExpireAt(System.currentTimeMillis() + 10000000000L);
        userRedisAccessToken.setTtl(System.currentTimeMillis() + 10000000000L);
        userRedisAccessTokenRepository.save(userRedisAccessToken);

        mockMvc.perform(
                delete("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "access-test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNull(response.getErrors());
            assertEquals("OK", response.getData());

            User userDb = userRepository.findById("test").orElse(null);
            assertNotNull(userDb);

            UserRedisAccessToken userRedisDB = userRedisAccessTokenRepository.findById("access-test").orElse(null);
            assertNull(userRedisDB);
        });
    }
}