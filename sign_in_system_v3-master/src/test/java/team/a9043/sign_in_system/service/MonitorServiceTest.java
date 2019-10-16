package team.a9043.sign_in_system.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.PageInfo;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.mapper.SisScheduleMapper;
import team.a9043.sign_in_system.pojo.*;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;
import team.a9043.sign_in_system.util.judgetime.JudgeTimeUtil;
import team.a9043.sign_in_system.util.judgetime.ScheduleParserException;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutionException;

/**
 * @author a9043
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class MonitorServiceTest {
    private ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private MonitorService monitorService;
    @Resource
    private SisScheduleMapper sisScheduleMapper;

    @Test
    public void getCourses() throws ExecutionException, InterruptedException,
        JsonProcessingException {
        SisUser sisUser = new SisUser();
        sisUser.setSuId("2016220401001");
        PageInfo<SisCourse> pageInfo = monitorService.getCourses(sisUser);
        log.info(objectMapper.writeValueAsString(pageInfo));
    }

    @Test
    @Transactional
    public void insertSupervision() throws IncorrectParameterException,
        ScheduleParserException, InvalidPermissionException,
        InvalidTimeParameterException, JsonProcessingException {
        SisUser sisUser = new SisUser();
        sisUser.setSuId("2016220401001");

        Integer ssId = 2;

        SisSchedule sisSchedule = new SisSchedule();
        sisSchedule.setSsId(ssId);
        sisSchedule.setSsDayOfWeek(DayOfWeek.TUESDAY.getValue());
        sisSchedule.setSsStartTime(1);

        SisSupervision sisSupervision = new SisSupervision();
        sisSupervision.setSsId(ssId);
        sisSupervision.setSsvWeek(1);
        sisSupervision.setSsvActualNum(1);
        sisSupervision.setSsvMobileNum(1);
        sisSupervision.setSsvSleepNum(1);
        sisSupervision.setSsvRecInfo(":");

        LocalDate localDate = JudgeTimeUtil.getScheduleDate(sisSchedule, 1);
        LocalTime localTime =
            JudgeTimeUtil.getClassTime(sisSchedule.getSsStartTime());

        LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
        OperationResponse or = monitorService.insertSupervision(sisUser, ssId,
            sisSupervision,
            localDateTime);
        log.info(objectMapper.writeValueAsString(or));
    }

    @Test
    public void getSupervisions() {
    }

    @Test
    public void modifyMonitor() {
    }

    @Test
    @Transactional
    public void applyForTransfer() throws IncorrectParameterException,
        InvalidPermissionException {
        SisUser sisUser = new SisUser();
        sisUser.setSuId("2016220401001");

        SisUser tSisUser = new SisUser();
        tSisUser.setSuId("Z");

        SisMonitorTrans sisMonitorTrans = new SisMonitorTrans();
        sisMonitorTrans.setSuId("2016220401001");
        sisMonitorTrans.setSsId(2);
        sisMonitorTrans.setSmtWeek(2);

        monitorService.applyForTransfer(sisUser, 2, sisMonitorTrans);
    }

    @Test
    public void getMonitors() throws IncorrectParameterException,
        JsonProcessingException {
        PageInfo<SisUser> pageInfo = monitorService.getMonitors(1, 10,
            "2016220401001", null, true);
        log.info(objectMapper.writeValueAsString(pageInfo));
    }

    @Test
    public void isCourseTime() throws InvalidTimeParameterException,
        ScheduleParserException {
        SisSchedule sisSchedule = sisScheduleMapper.selectByPrimaryKey(4);
        JudgeTimeUtil.isCourseTime(sisSchedule, 2, LocalDateTime.now());
    }
}