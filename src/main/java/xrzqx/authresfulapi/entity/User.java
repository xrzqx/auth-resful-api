package xrzqx.authresfulapi.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    private String username;

    private String password;

    private String name;

    private String email;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "refresh_token_expired_at")
    private Long refreshTokenExpiredAt;

    @Column(name = "access_token")
    private String accessToken;

    @Column(name = "access_token_expired_at")
    private Long accessTokenExpiredAt;

}
