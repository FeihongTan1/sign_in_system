package team.a9043.sign_in_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import team.a9043.sign_in_system.mapper.SisUserMapper;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sis_message_system.service_pojo.FormId;
import team.a9043.sis_message_system.service_pojo.SignInMessage;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
@Slf4j
public class MessageService {
    @Value("${rbmq.signIn.exchange}")
    private String signInExchange;
    @Value("${rbmq.signIn.bindKey}")
    private String signInBindKey;
    @Resource
    private SisUserMapper sisUserMapper;
    @Resource(name = "sisRedisTemplate")
    private RedisTemplate<String, Object> sisRedisTemplate;
    @Resource
    private AmqpTemplate amqpTemplate;

    public void receiveFormId(SisUser sisUser, String formIdStr) {
        SisUser stdUser = sisUserMapper.selectByPrimaryKey(sisUser.getSuId());
        if (null == stdUser || null == stdUser.getSuOpenid() || stdUser.getSuOpenid().isEmpty()) return;

        String formIdKeyFormat = "sis_formid_openid_%s";
        String key = String.format(formIdKeyFormat, stdUser.getSuOpenid());
        FormId formId = new FormId(formIdStr, LocalDateTime.now().plus(6, ChronoUnit.DAYS));

        sisRedisTemplate.opsForList().leftPush(key, formId);
        sisRedisTemplate.opsForList().trim(key, 0, 50);
        log.info("success add: " + stdUser.getSuOpenid() + " , " + formIdStr);
    }

    @SuppressWarnings("ConstantConditions")
    @Async
    public void sendSignInMessage(Integer ssId, LocalDateTime signInEndTime) {
        SignInMessage signInMessage = new SignInMessage(ssId, signInEndTime);
        amqpTemplate.convertAndSend(signInExchange, signInBindKey, signInMessage);
        log.info(String.format("send Message success: true in ssId %s", ssId));
    }
}
