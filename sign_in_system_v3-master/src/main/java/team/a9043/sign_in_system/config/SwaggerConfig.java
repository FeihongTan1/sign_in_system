package team.a9043.sign_in_system.config;

import com.google.common.base.Predicates;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * @author a9043
 */
@Configuration
@EnableSwagger2
public class SwaggerConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("swagger-ui.html")
            .addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**")
            .addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Bean
    public Docket createRestApi() {
        //noinspection Guava
        return new Docket(DocumentationType.SWAGGER_2)
            .host("118.126.111.189")
            .apiInfo(apiInfo())
            .select()
            .apis(RequestHandlerSelectors.basePackage(
                "team.a9043.sign_in_system"))
            .paths(PathSelectors.any())
            .paths(Predicates.not(PathSelectors.regex("/error.*")))
            .build();
    }

    private ApiInfo apiInfo() {
        Contact contact = new Contact(
            "a9043",
            "https://lohoknang.blog/",
            "zzz13129180808@gmail.com");
        return new ApiInfoBuilder()
            .title("Sign In System APIs")
            .description("https://github.com/759434091")
            .termsOfServiceUrl("https://github.com/759434091/")
            .contact(contact)
            .version("0.0.1")
            .build();
    }
}
