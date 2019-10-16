package team.a9043.sign_in_system.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Mvc 全局设置
 *
 * @author a9043
 */
@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new HandlerInterceptor() {
            @Override
            public boolean preHandle(HttpServletRequest request,
                                     HttpServletResponse response,
                                     Object handler) {
                response.setDateHeader(HttpHeaders.EXPIRES, -1);
                response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
                response.setHeader(HttpHeaders.PRAGMA, "no-cache");
                return true;
            }
        }).addPathPatterns("/**");
    }
}
