package team.a9043.sign_in_system.service_pojo;

import lombok.Getter;
import lombok.Setter;
import team.a9043.sign_in_system.pojo.SisUser;

/**
 * @author a9043
 */
@Getter
@Setter
public class TokenResult {
    private String accessToken;
    private SisUser sisUser;
}
