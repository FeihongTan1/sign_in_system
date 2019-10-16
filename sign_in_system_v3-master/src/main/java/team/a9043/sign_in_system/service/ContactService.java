package team.a9043.sign_in_system.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidSuccessOperationResponse;
import team.a9043.sis_message_system.service_pojo.SisContact;

import javax.annotation.Resource;

@Service
@Slf4j
public class ContactService {
    @Value("${rbmq.mail.exchange}")
    private String mailExchange;
    @Value("${rbmq.mail.bindKey}")
    private String mailBindKey;
    @Resource
    private AmqpTemplate amqpTemplate;

    public VoidOperationResponse receiveKssContact(SisContact sisContact) {
        amqpTemplate.convertAndSend(mailExchange, mailBindKey, sisContact);
        return VoidSuccessOperationResponse.SUCCESS;
    }
}
