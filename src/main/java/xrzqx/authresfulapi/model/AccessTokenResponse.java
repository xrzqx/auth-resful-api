package xrzqx.authresfulapi.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AccessTokenResponse {

    private String accessToken;

    private Long expiredAt;
}
