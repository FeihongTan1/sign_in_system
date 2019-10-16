package team.a9043.sign_in_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.pojo.SisCourse;
import team.a9043.sign_in_system.pojo.SisSignInDetail;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.service_pojo.OperationResponse;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author a9043
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SignInServiceTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private SignInService signInService;
    @Resource(name = "sisRedisTemplate")
    private RedisTemplate<String, Object> sisRedisTemplate;

    @Test
    public void getSignInsUser() throws IncorrectParameterException,
        JsonProcessingException {
        SisUser sisUser = new SisUser();
        sisUser.setSuId("2016220401001");
        SisCourse sc = signInService.getSignIns(sisUser, "A");
        log.info(objectMapper.writeValueAsString(sc));
    }

    @Test
    public void getSignIn() throws JsonProcessingException {
        OperationResponse or = signInService.getSignIn(2, 1);
        log.info(objectMapper.writeValueAsString(or));
    }

    @Test
    public void getSignIns() throws IncorrectParameterException,
        JsonProcessingException {
        SisCourse sc = signInService.getSignIns("A");
        log.info(objectMapper.writeValueAsString(sc));
    }

    @Test
    public void testList() {
        Object obj = sisRedisTemplate.opsForList().rightPop("a");
        log.info("");
    }

    @Test
    public void testJson() throws IOException {
        File file = new File("/home/a9043/图片/IMG_5651.png");
        byte[] bytes = Files.readAllBytes(file.toPath());
        SisSignInDetail sisSignInDetail = new SisSignInDetail();
        sisSignInDetail.setSsiId(1);
        sisSignInDetail.setSsidPicture(bytes);
        String s = objectMapper.writeValueAsString(sisSignInDetail);
        log.info(s);
    }


    @Test
    public void test7() {
        Object o = sisRedisTemplate.opsForHash().get("sis_ssId_506_week_10", "2016220401007");
        log.info(o.toString());
    }
}