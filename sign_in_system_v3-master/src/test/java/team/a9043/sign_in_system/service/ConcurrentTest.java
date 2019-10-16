package team.a9043.sign_in_system.service;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import team.a9043.sign_in_system.mapper.SisJoinCourseMapper;
import team.a9043.sign_in_system.mapper.SisScheduleMapper;
import team.a9043.sign_in_system.pojo.SisJoinCourse;
import team.a9043.sign_in_system.pojo.SisJoinCourseExample;
import team.a9043.sign_in_system.pojo.SisSchedule;
import team.a9043.sign_in_system.pojo.SisScheduleExample;
import team.a9043.sign_in_system.util.JwtUtil;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

/**
 * @author a9043
 */
@SuppressWarnings("Duplicates")
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class ConcurrentTest {
    @Resource
    private SisJoinCourseMapper sisJoinCourseMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;
    private RestTemplate restTemplate;

    public ConcurrentTest() {
        RestTemplateBuilder restTemplateBuilder = new RestTemplateBuilder();
        this.restTemplate = restTemplateBuilder
            .build();
    }

    @Test
    public void getCourse() {
        List<String> scIdList = Arrays.asList(
            "E0901330.01",
            "E0901330.02",
            "E0901330.03",
            "E0901330.04"
        );
        SisJoinCourseExample sisJoinCourseExample = new
            SisJoinCourseExample();
        sisJoinCourseExample.createCriteria()
            .andScIdIn(scIdList)
            .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.ATTENDANCE
                .ordinal());
        List<SisJoinCourse> sisJoinCourseList =
            sisJoinCourseMapper.selectByExample(sisJoinCourseExample);

        log.info("total: " + sisJoinCourseList
            .parallelStream().count());
        sisJoinCourseList
            .parallelStream()
            .forEach(sjc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("suId", sjc.getSuId());
                map.put("suName", "");
                map.put("suAuthoritiesStr", "STUDENT");
                map.put("type", "code");

                HttpHeaders headers = new HttpHeaders();
                try {
                    headers.add("Access-Token", getAccessToken());
                } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                    BadPaddingException | InvalidKeyException |
                    IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
                headers.add("Authorization",
                    "Bearer " + JwtUtil.createJWT(map));

/*                try {
                    ResponseEntity<String> jsonObject1 =
                        restTemplate.exchange(
                            "http://118.126.111.189:8088" +
                                "/courses?getType=student",
                            HttpMethod.GET,
                            new HttpEntity<String>(headers),
                            String.class,
                            new HashMap<>());
                    log.info(jsonObject1.getBody());
                } catch (HttpClientErrorException e) {
                    log.error(new String(e.getResponseBodyAsByteArray
                        ()));
                }*/
            });

    }

    @Test
    public void test() {
        List<String> scIdList = Arrays.asList(
            "E0911835.01", "E0911835.02", "E0911835.03", "E0911835.04",
            "E0911835.05", "E0911835.06", "E0911835.07", "E0901040.01",
            "E0901040.02", "E0901040.03", "E0901040.04", "E0901040.05",
            "E0901040.06", "E0901040.07"
        );
        SisScheduleExample sisScheduleExample = new SisScheduleExample();
        sisScheduleExample.createCriteria().andScIdIn(scIdList);
        List<SisSchedule> sisScheduleList =
            sisScheduleMapper.selectByExample(sisScheduleExample);

        Map<String, Object> claimsMap = new HashMap<>();
        claimsMap.put("suId", "2016220401000");
        claimsMap.put("suName", "");
        claimsMap.put("suAuthoritiesStr", "ADMINISTRATOR, TEACHER");
        claimsMap.put("type", "code");
        HttpHeaders httpHeaders = new HttpHeaders();
        try {
            httpHeaders.add("Access-Token", getAccessToken());
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | BadPaddingException | InvalidKeyException | IllegalBlockSizeException e) {
            e.printStackTrace();
        }
        httpHeaders.add("Authorization",
            "Bearer " + JwtUtil.createJWT(claimsMap));

        sisScheduleList
            .forEach(s -> {
                try {
                    String jsonObject1 =
                        restTemplate.postForObject(String.format(
                            "https://api.xsix103.cn/sign_in_system/v3" +
                                "/schedules/%s/signIns",
                            s.getSsId()),
                            new HttpEntity<String>(httpHeaders),
                            String.class);
                    log.info(jsonObject1);
                } catch (HttpClientErrorException e) {
                    log.error(new String(e.getResponseBodyAsByteArray()));
                }
            });

        SisJoinCourseExample sisJoinCourseExample = new
            SisJoinCourseExample();
        sisJoinCourseExample.createCriteria()
            .andScIdIn(scIdList)
            .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.ATTENDANCE
                .ordinal());
        List<SisJoinCourse> sisJoinCourseList =
            sisJoinCourseMapper.selectByExample(sisJoinCourseExample);

        log.info("total: " + sisJoinCourseList
            .parallelStream().count());
        sisJoinCourseList
            .parallelStream()

            .forEach(sjc -> {
                Map<String, Object> map = new HashMap<>();
                map.put("suId", sjc.getSuId());
                map.put("suName", "");
                map.put("suAuthoritiesStr", "STUDENT");
                map.put("type", "code");

                HttpHeaders headers = new HttpHeaders();
                try {
                    headers.add("Access-Token", getAccessToken());
                } catch (NoSuchPaddingException | NoSuchAlgorithmException |
                    BadPaddingException | InvalidKeyException |
                    IllegalBlockSizeException e) {
                    e.printStackTrace();
                }
                headers.add("Authorization",
                    "Bearer " + JwtUtil.createJWT(map));

                sisScheduleList
                    .stream()
                    .filter(s -> s.getScId().equals(sjc.getScId()))
                    .forEach(s -> {
                        try {
                            String jsonObject1 =
                                restTemplate.postForObject(String.format(
                                    "https://api.xsix103.cn/sign_in_system/v3" +
                                        "/schedules/%s/signIns/doSignIn",
                                    s.getSsId()),
                                    new HttpEntity<String>(headers),
                                    String.class);
                            log.info(jsonObject1);
                        } catch (HttpClientErrorException e) {
                            log.error(new String(e.getResponseBodyAsByteArray
                                ()));
                        }
                    });
            });

    }

    public String getAccessToken() throws NoSuchPaddingException,
        NoSuchAlgorithmException, InvalidKeyException, BadPaddingException,
        IllegalBlockSizeException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("loc_lat", (double) 1);
        jsonObject.put("loc_long", (double) 1);

        byte[] bytes = Base64.getDecoder()
            .decode("JWmPJIqFj+Lxu4GbO/RP7w==");
        SecretKeySpec secretKeySpec =
            new SecretKeySpec(bytes, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        return Base64.getEncoder().encodeToString(cipher.doFinal(
            jsonObject.toString().getBytes()));
    }
}
