package team.a9043.sign_in_system.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.client.RestTemplate;
import team.a9043.sign_in_system.convertor.JsonObjectHttpMessageConverter;
import team.a9043.sign_in_system.exception.WxServerException;
import team.a9043.sign_in_system.service_pojo.AppToken;

import javax.annotation.Resource;
import java.util.Calendar;

@Configuration
@Slf4j
public class WxAppConfig {
    @Value("${wxapp.appid}")
    private String appid;
    @Value("${wxapp.secret}")
    private String secret;
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Resource
    private ObjectMapper objectMapper;
    private RestTemplate restTemplate;
    private AppToken appToken;

    @Bean("restTemplate")
    public RestTemplate getRestTemplate(@Value("${wxapp.rooturl}") String rooturl,
                                        @Autowired JsonObjectHttpMessageConverter jsonObjectHttpMessageConverter) {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        restTemplate = restTemplateBuilder
            .additionalMessageConverters(jsonObjectHttpMessageConverter)
            .rootUri(rooturl)
            .build();
        return restTemplate;
    }

    @Bean
    @DependsOn("restTemplate")
    public AppToken initAppToken(@Value("${wxapp.appid}") String appid,
                                 @Value("${wxapp.secret}") String secret) throws JsonProcessingException {
        String urlFormat = "/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
        JSONObject jsonObject = restTemplate.getForObject(String.format(urlFormat, appid, secret), JSONObject.class);
        if (null == jsonObject)
            throw new WxServerException("can not get response");
        if (!jsonObject.has("access_token"))
            throw new WxServerException(jsonObject.toString());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, jsonObject.getInt("expires_in"));
        calendar.add(Calendar.MINUTE, -5);

        appToken = new AppToken(jsonObject.getString("access_token"), calendar.getTime());

        threadPoolTaskScheduler.schedule(this::updateAppToken, calendar.toInstant());
        log.info("success init AppToken: " + objectMapper.writeValueAsString(appToken));
        return appToken;
    }

    private void updateAppToken() {
        String urlFormat = "/cgi-bin/token?grant_type=client_credential&appid=%s&secret=%s";
        JSONObject jsonObject = restTemplate.getForObject(String.format(urlFormat, appid, secret), JSONObject.class);
        if (null == jsonObject)
            throw new WxServerException("can not get response");
        if (!jsonObject.has("access_token"))
            throw new WxServerException(jsonObject.toString());

        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, jsonObject.getInt("expires_in"));
        calendar.add(Calendar.MINUTE, -5);

        appToken.modifyAppToken(jsonObject.getString("access_token"), calendar.getTime());

        threadPoolTaskScheduler.schedule(this::updateAppToken, calendar.toInstant());
        try {
            log.info("success update AppToken: " + objectMapper.writeValueAsString(appToken));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}
