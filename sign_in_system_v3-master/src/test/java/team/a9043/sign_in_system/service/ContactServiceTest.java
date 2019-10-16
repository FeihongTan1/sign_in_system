package team.a9043.sign_in_system.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author a9043
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ContactServiceTest {
    @Resource
    private MailSender mailSender;

    @Test
    public void test() {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom("2541175183@qq.com");
        mailMessage.setSubject("subject");
        mailMessage.setText("test");
        mailMessage.setTo("a90434957@live.cn");
        mailSender.send(mailMessage);
    }
}
