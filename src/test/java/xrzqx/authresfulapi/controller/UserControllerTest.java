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
import xrzqx.authresfulapi.model.RegisterUserRequest;
import xrzqx.authresfulapi.model.UpdateUserRequest;
import xrzqx.authresfulapi.model.UserResponse;
import xrzqx.authresfulapi.model.WebResponse;
import xrzqx.authresfulapi.repository.UserRedisAccessTokenRepository;
import xrzqx.authresfulapi.repository.UserRepository;
import xrzqx.authresfulapi.security.BCrypt;

import static org.junit.jupiter.api.Assertions.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.MockMvcBuilder.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

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
    void testRegisterSuccess() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("test");
        request.setPassword("rahasia");
        request.setEmail("test@mail.com");
        request.setName("Test");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertEquals("OK", response.getData());
        });
    }

    @Test
    void testRegisterBadRequest() throws Exception {
        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("");
        request.setPassword("");
        request.setName("");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    void testRegisterDuplicate() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Test");
        user.setEmail("test@mail.com");
        userRepository.save(user);

        RegisterUserRequest request = new RegisterUserRequest();
        request.setUsername("test");
        request.setPassword("rahasia");
        request.setName("Test");
        request.setEmail("test@mail.com");

        mockMvc.perform(
                post("/api/users")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isBadRequest()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    void getUserUnauthorizedTokenNotSend() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
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
    void getUserUnauthorized() throws Exception {
        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "notfound")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    void getUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Test");
        user.setEmail("test@mail.com");
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
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
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "access-test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("test", response.getData().getUsername());
            assertEquals("Test", response.getData().getName());
        });
    }

    @Test
    void getUserTokenExpired() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Test");
        user.setEmail("test@mail.com");
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
        userRepository.save(user);

        mockMvc.perform(
                get("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("X-API-TOKEN", "access-test")
        ).andExpectAll(
                status().isUnauthorized()
        ).andDo(result -> {
            WebResponse<String> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNotNull(response.getErrors());
        });
    }

    @Test
    void updateUserUnauthorized() throws Exception {
        UpdateUserRequest request = new UpdateUserRequest();

        mockMvc.perform(
                patch("/api/users/current")
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
    void updateUserSuccess() throws Exception {
        User user = new User();
        user.setUsername("test");
        user.setPassword(BCrypt.hashpw("rahasia", BCrypt.gensalt()));
        user.setName("Test");
        user.setEmail("test@mail.com");
        user.setRefreshToken("refresh-test");
        user.setRefreshTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
        user.setAccessToken("access-test");
        user.setAccessTokenExpiredAt(System.currentTimeMillis() + 10000000000L);
        userRepository.save(user);

        UserRedisAccessToken userRedisAccessToken = new UserRedisAccessToken();
        userRedisAccessToken.setId(user.getAccessToken());
        userRedisAccessToken.setUsername(user.getUsername());
        userRedisAccessToken.setName(user.getName());
        userRedisAccessToken.setEmail(user.getEmail());
        userRedisAccessToken.setExpireAt(System.currentTimeMillis() + 10000000000L);
        userRedisAccessToken.setTtl(System.currentTimeMillis() + 10000000000L);
        userRedisAccessTokenRepository.save(userRedisAccessToken);

        UpdateUserRequest request = new UpdateUserRequest();
        request.setName("razzaq");
        request.setPassword("razzaq123");

        mockMvc.perform(
                patch("/api/users/current")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("X-API-TOKEN", "access-test")
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });

            assertNull(response.getErrors());
            assertEquals("razzaq", response.getData().getName());
            assertEquals("test", response.getData().getUsername());

            User userDb = userRepository.findById("test").orElse(null);
            assertNotNull(userDb);
            assertTrue(BCrypt.checkpw("razzaq123", userDb.getPassword()));
        });
    }

}