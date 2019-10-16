package team.a9043.sign_in_system.security.tokenuser;

import springfox.documentation.annotations.ApiIgnore;

import java.lang.annotation.*;

/**
 * 获得Token中的 SisUser
 *
 * @author a9043
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@ApiIgnore
@Documented
public @interface TokenUser {
}
