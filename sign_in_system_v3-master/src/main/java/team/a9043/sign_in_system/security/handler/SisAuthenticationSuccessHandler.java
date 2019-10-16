package team.a9043.sign_in_system.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.entity.SecurityUserDetails;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.service_pojo.TokenResult;
import team.a9043.sign_in_system.util.JwtUtil;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class SisAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    @Resource
    private ObjectMapper objectMapper;

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        SecurityUserDetails securityUserDetails =
            (SecurityUserDetails) authentication.getPrincipal();
        SisUser sisUser = securityUserDetails.getSisUser();
        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("suId", sisUser.getSuId());
        claimsMap.put("suName", sisUser.getSuName());
        claimsMap.put("suAuthoritiesStr", sisUser.getSuAuthoritiesStr());
        claimsMap.put("type", "password");

        String token = JwtUtil.createJWT(claimsMap);

        sisUser.setSuPassword(null);
        TokenResult tokenResult = new TokenResult();
        tokenResult.setSisUser(sisUser);
        tokenResult.setAccessToken(token);
        response.setHeader("Content-type", "application/json;charset=utf-8");
        OperationResponse<TokenResult> operationResponse =
            new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(tokenResult);
        operationResponse.setMessage("data => tokenResult");
        objectMapper.writeValue(response.getWriter(), operationResponse);
    }
}
