package team.a9043.sign_in_system.other;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import team.a9043.sign_in_system.mapper.SisJoinCourseMapper;
import team.a9043.sign_in_system.mapper.SisScheduleMapper;
import team.a9043.sign_in_system.pojo.*;
import team.a9043.sign_in_system.service_pojo.OperationResponse;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * @author a9043
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class OtherTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private SisJoinCourseMapper sisJoinCourseMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;
    @Resource
    private TaskExecutor taskExecutor;
    @Resource
    private ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Resource
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Test
    public void test() {
        List<String> scIdList = new ArrayList<>();
        scIdList.add("A");

        CompletableFuture<List<SisJoinCourse>> listCompletableFuture =
            CompletableFuture.supplyAsync(() -> {
                SisJoinCourseExample sisJoinCourseExample =
                    new SisJoinCourseExample();
                sisJoinCourseExample.createCriteria().andScIdIn(scIdList);
                return sisJoinCourseMapper.selectByExample(sisJoinCourseExample);
            }, taskExecutor).toCompletableFuture();
        CompletableFuture<List<SisSchedule>> listCompletableFuture1 =
            CompletableFuture.supplyAsync(() -> {
                SisScheduleExample sisScheduleExample =
                    new SisScheduleExample();
                sisScheduleExample.createCriteria().andScIdIn(scIdList);
                return sisScheduleMapper.selectByExample(sisScheduleExample);
            }, taskExecutor).toCompletableFuture();

        CompletableFuture.allOf(listCompletableFuture,
            listCompletableFuture1).join();

        log.info("end");
    }

    @Test
    public void test2() {
        AtomicBoolean isEnd = new AtomicBoolean();
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.SECOND, 5);
        threadPoolTaskScheduler.schedule(() -> isEnd.set(true),
            calendar.toInstant());

        while (!isEnd.get()) {

        }
        log.info("end: " + isEnd);
    }

    @Test
    public void test3() {
        String ssSuspension = "1,a,2,c.,-1";
        List<Integer> integers = Arrays.stream(ssSuspension.split(","))
            .map(String::trim)
            .map(s -> {
                try {
                    return Integer.valueOf(s);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .filter(integer -> integer > 0)
            .collect(Collectors.toList());
        log.info(new JSONArray(integers).toString(2));
    }

    @Test
    public void teat4() throws NoSuchAlgorithmException,
        NoSuchPaddingException, InvalidKeyException, BadPaddingException,
        IllegalBlockSizeException {
        //do
        byte[] bytes = Base64.getDecoder()
            .decode("JWmPJIqFj+Lxu4GbO/RP7w==");
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            stringBuilder.append(Integer.toString(b, 2));
        }
        log.info(stringBuilder.toString().replaceAll("-", ""));
        log.info(new JSONArray(bytes).toString());
        SecretKeySpec secretKeySpec =
            new SecretKeySpec(bytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        String enStr = Base64.getEncoder().encodeToString(cipher.doFinal(
            "123456".getBytes()));

        log.info(enStr);
        Cipher cipher1 = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        log.info(new String(cipher.doFinal(Base64.getDecoder().decode(
            "a3pk8E4cTWyFdUz9E5VcyQ=="))));
    }

    @Test
    public void test5() {
        log.info(bCryptPasswordEncoder.encode("123456"));
    }

    @Test
    public void test6() throws JsonProcessingException {
        SisUser sisUser = new SisUser();
        OperationResponse operationResponse = new OperationResponse();
        operationResponse.setSuccess(true);
        operationResponse.setCode(0);
        operationResponse.setData(sisUser);
        log.info(objectMapper.writeValueAsString(operationResponse));
    }
}