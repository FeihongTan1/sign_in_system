package team.a9043.sign_in_system.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.entity.SisAuthenticationToken;
import team.a9043.sign_in_system.util.JwtUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SisAuthenticationFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = header.substring(7);
        if (token.length() <= 0 ||
            SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims;
        SisUser sisUser;
        //解析token
        try {
            claims = JwtUtil.parseJwt(token);
            sisUser = new SisUser();
            sisUser.setSuId(claims.get("suId", String.class));
            sisUser.setSuName(claims.get("suName", String.class));
            sisUser.setType(claims.get("type", String.class));
            sisUser.setSuAuthoritiesStr(claims.get("suAuthoritiesStr",
                String.class));

        } catch (MalformedJwtException | SignatureException | ExpiredJwtException e) {
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }

        //设定Authentication
        SisAuthenticationToken sisAuthenticationToken =
            new SisAuthenticationToken(sisUser);
        sisAuthenticationToken
            .setDetails(
                new WebAuthenticationDetailsSource().
                    buildDetails(
                        request));
        sisAuthenticationToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(sisAuthenticationToken);

        filterChain.doFilter(request, response);
    }
}
