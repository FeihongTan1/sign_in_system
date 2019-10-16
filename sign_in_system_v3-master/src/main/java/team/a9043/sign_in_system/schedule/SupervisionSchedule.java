package team.a9043.sign_in_system.schedule;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.json.JSONArray;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import team.a9043.sign_in_system.mapper.SisCourseMapper;
import team.a9043.sign_in_system.mapper.SisScheduleMapper;
import team.a9043.sign_in_system.mapper.SisSupervisionMapper;
import team.a9043.sign_in_system.mapper.SisUserInfoMapper;
import team.a9043.sign_in_system.pojo.*;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;
import team.a9043.sign_in_system.util.judgetime.JudgeTimeUtil;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SupervisionSchedule {
    @Resource
    private SisCourseMapper sisCourseMapper;
    @Resource
    private SisSupervisionMapper sisSupervisionMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;
    @Resource
    private SisUserInfoMapper sisUserInfoMapper;

    @Scheduled(cron = "0 0 0 * * ?")
    public void supervisionSchedule() {
        try {
            int week = JudgeTimeUtil.getWeek(LocalDate.now()) - 1;

            //获得督导课程
            SisCourseExample sisCourseExample = new SisCourseExample();
            sisCourseExample.createCriteria().andScNeedMonitorEqualTo(true).andSuIdIsNotNull();
            List<SisCourse> sisCourseList =
                sisCourseMapper.selectByExample(sisCourseExample);

            //获得课程排课
            List<String> scIdList = sisCourseList.stream()
                .map(SisCourse::getScId)
                .distinct()
                .collect(Collectors.toList());
            if (scIdList.isEmpty())
                return;
            SisScheduleExample sisScheduleExample = new SisScheduleExample();
            sisScheduleExample.createCriteria().andScIdIn(scIdList);
            List<SisSchedule> sisScheduleList =
                sisScheduleMapper.selectByExample(sisScheduleExample);

            //获得督导历史
            List<Integer> ssIdList =
                sisScheduleList.stream().map(SisSchedule::getSsId).distinct().collect(Collectors.toList());
            if (ssIdList.isEmpty())
                return;

            SisSupervisionExample sisSupervisionExample =
                new SisSupervisionExample();
            sisSupervisionExample.createCriteria().andSsIdIn(ssIdList).andSsvWeekLessThanOrEqualTo(week);
            List<SisSupervision> sisSupervisionList =
                sisSupervisionMapper.selectByExample(sisSupervisionExample);

            List<SisUserInfo> sisUserInfoList = sisCourseList.stream()
                .map(sisCourse -> {
                    String scId = sisCourse.getScId();
                    String suId = sisCourse.getSuId();

                    List<SisSchedule> scheduleList =
                        sisScheduleList.stream()
                            .filter(sisSchedule -> sisSchedule.getScId().equals(scId))
                            .collect(Collectors.toList());

                    if (scheduleList.isEmpty())
                        return null;
                    int totalLackNum = scheduleList.stream()
                        .mapToInt(sisSchedule -> {
                            Integer ssId = sisSchedule.getSsId();
                            int countNum =
                                (int) sisSupervisionList.stream()
                                    .filter(sisSupervision -> sisSupervision.getSsId().equals(ssId))
                                    .count();

                            int lackNum = week - countNum;
                            lackNum = lackNum < 0 ? 0 : lackNum;
                            return lackNum;
                        })
                        .sum();
                    SisUserInfo sisUserInfo = new SisUserInfo();
                    sisUserInfo.setSuId(suId);
                    sisUserInfo.setSuiLackNum(totalLackNum);
                    return sisUserInfo;
                })
                .collect(ArrayList::new,
                    (list, sisUserInfo) -> {
                        int idx = list.indexOf(sisUserInfo);
                        if (-1 == idx) {
                            list.add(sisUserInfo);
                            return;
                        }

                        SisUserInfo stdSisUserInfo = list.get(idx);
                        stdSisUserInfo.setSuiLackNum(stdSisUserInfo.getSuiLackNum() + sisUserInfo.getSuiLackNum());
                    },
                    (arr1, arr2) -> arr2.forEach(sisUserInfo -> {
                        int idx = arr1.indexOf(sisUserInfo);
                        if (-1 == idx) {
                            arr1.add(sisUserInfo);
                            return;
                        }

                        SisUserInfo stdSisUserInfo = arr1.get(idx);
                        stdSisUserInfo.setSuiLackNum(stdSisUserInfo.getSuiLackNum() + sisUserInfo.getSuiLackNum());
                    }));

            sisUserInfoMapper.insertList(sisUserInfoList);
            log.info("insert sisUserInfo success: " + new JSONArray(sisUserInfoList));
        } catch (InvalidTimeParameterException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void removeTmp() throws IOException {
        String pathStr = "/home/sis-user/javaApps/sign_in_system/temp/imgs";
        File file = new File(pathStr);
        if (!file.exists()) return;

        FileUtils.deleteDirectory(file);
    }
}
