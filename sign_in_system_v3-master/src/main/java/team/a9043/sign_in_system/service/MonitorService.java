package team.a9043.sign_in_system.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.mapper.*;
import team.a9043.sign_in_system.pojo.*;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidSuccessOperationResponse;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;
import team.a9043.sign_in_system.util.judgetime.JudgeTimeUtil;
import team.a9043.sign_in_system.util.judgetime.ScheduleParserException;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

/**
 * @author a9043
 */
@Service
@Slf4j
public class MonitorService {
    private CopyOnWriteArraySet<String> copyOnWriteArraySet =
        new CopyOnWriteArraySet<>();
    @Resource
    private SisCourseMapper sisCourseMapper;
    @Resource
    private SisUserMapper sisUserMapper;
    @Resource
    private SisScheduleMapper sisScheduleMapper;
    @Resource
    private SisSupervisionMapper sisSupervisionMapper;
    @Resource
    private SisMonitorTransMapper sisMonitorTransMapper;
    @Resource
    private SisUserInfoMapper sisUserInfoMapper;
    @Resource
    private SisJoinCourseMapper sisJoinCourseMapper;

    public PageInfo<SisCourse> getCourses(@NotNull SisUser sisUser) {
        SisCourseExample sisCourseExample = new SisCourseExample();
        sisCourseExample.createCriteria().andSuIdEqualTo(sisUser.getSuId());

        List<SisCourse> sisCourseList =
            sisCourseMapper.selectByExample(sisCourseExample);
        PageInfo<SisCourse> sisCoursePageInfo = new PageInfo<>(sisCourseList);
        if (sisCourseList.isEmpty()) return sisCoursePageInfo;

        List<String> scIdList = sisCourseList.stream()
            .map(SisCourse::getScId)
            .collect(Collectors.toList());

        // join joinCourse
        SisJoinCourseExample sisJoinCourseExample = new SisJoinCourseExample();
        sisJoinCourseExample.createCriteria()
            .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.TEACHING.ordinal())
            .andScIdIn(scIdList);
        List<SisJoinCourse> sisJoinCourseList =
            sisJoinCourseMapper.selectByExample(sisJoinCourseExample);

        // join schedule
        SisScheduleExample sisScheduleExample = new SisScheduleExample();
        sisScheduleExample.createCriteria().andScIdIn(scIdList);
        List<SisSchedule> sisScheduleList =
            sisScheduleMapper.selectByExample(sisScheduleExample);

        List<String> suIdList = sisJoinCourseList.stream()
            .map(SisJoinCourse::getSuId)
            .collect(Collectors.toList());

        // join sisUser
        List<SisUser> sisUserList =
            CourseService.getSisUserBySuIdList(suIdList, sisUserMapper);

        //merge joinCourse
        sisJoinCourseList.forEach(j -> j.setSisUser(sisUserList.stream()
            .filter(u -> u.getSuId().equals(j.getSuId()))
            .peek(u -> u.setSuPassword(null))
            .findAny()
            .orElse(null)));

        //merge sisCourse
        sisCoursePageInfo.getList().forEach(c -> {
            c.setSisScheduleList(sisScheduleList.stream()
                .filter(sisSchedule -> sisSchedule.getScId().equals(c.getScId()))
                .collect(Collectors.toList()));

            c.setSisJoinCourseList(sisJoinCourseList.stream()
                .filter(j -> j.getScId().equals(c.getScId()))
                .collect(Collectors.toList()));
        });

        if (log.isDebugEnabled()) {
            log.debug("User " + sisUser.getSuId() + " get course. ");
        }
        return sisCoursePageInfo;
    }

    public List<SisSchedule> getSupervisions(@NotNull SisUser sisUser,
                                             @NotNull String scId) throws IncorrectParameterException, InvalidPermissionException {

        SisCourse sisCourse = Optional
            .ofNullable(sisCourseMapper.selectByPrimaryKey(scId))
            .orElseThrow(() -> new IncorrectParameterException("No course: " + scId));

        if (!sisUser.getSuAuthorities().contains(new SimpleGrantedAuthority(
            "ADMINISTRATOR")) &&
            !sisUser.getSuAuthorities().contains(new SimpleGrantedAuthority(
                "TEACHER")) &&
            !sisUser.getSuId().equals(sisCourse.getSuId())) {
            throw new InvalidPermissionException(
                "Invalid permission: " + scId);
        }

        //join schedule
        SisScheduleExample sisScheduleExample = new SisScheduleExample();
        sisScheduleExample.createCriteria().andScIdEqualTo(scId);
        List<SisSchedule> sisScheduleList =
            sisScheduleMapper.selectByExample(sisScheduleExample);

        List<Integer> ssIdList = sisScheduleList.stream()
            .map(SisSchedule::getSsId)
            .collect(Collectors.toList());
        if (ssIdList.isEmpty()) return sisScheduleList;

        SisSupervisionExample sisSupervisionExample =
            new SisSupervisionExample();
        sisScheduleExample.createCriteria().andSsIdIn(ssIdList);
        List<SisSupervision> sisSupervisionList =
            sisSupervisionMapper.selectByExample(sisSupervisionExample);

        //merge sisSchedule
        sisScheduleList.forEach(s -> s.setSisSupervisionList(sisSupervisionList.stream()
            .filter(sisSupervision -> sisSupervision.getSsId().equals(s.getSsId()))
            .collect(Collectors.toList())));

        if (log.isDebugEnabled()) {
            log.debug("User " + sisUser.getSuId() + " get supervisions: " + scId);
        }
        return sisScheduleList;
    }

    @Transactional
    public VoidOperationResponse drawMonitor(@NotNull SisUser sisUser,
                                             @NotNull String scId) throws IncorrectParameterException {
        String key = String.format("sis_draw_monitor_%s", scId);
        if (!copyOnWriteArraySet.add(key))
            return new VoidOperationResponse(false, "该课程已被领取");

        SisCourse sisCourse = Optional
            .ofNullable(sisCourseMapper.selectByPrimaryKey(scId))
            .orElseThrow(() -> new IncorrectParameterException(
                "No course: " + scId));

        if (null != sisCourse.getSuId())
            return new VoidOperationResponse(false, "该课程已被领取");

        SisCourse updatedSisCourse = new SisCourse();
        updatedSisCourse.setScId(sisCourse.getScId());
        updatedSisCourse.setSuId(sisUser.getSuId());

        sisCourseMapper.updateByPrimaryKeySelective(updatedSisCourse);
        log.info("User " + sisUser.getSuId() + " has draw course: " + scId);
        copyOnWriteArraySet.remove(key);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    @Transactional
    public VoidOperationResponse insertSupervision(@NotNull SisUser sisUser,
                                                   @NotNull Integer ssId,
                                                   @NotNull SisSupervision sisSupervision,
                                                   @NotNull LocalDateTime currentDateTime) throws IncorrectParameterException, ScheduleParserException, InvalidPermissionException, InvalidTimeParameterException {
        //check exist
        SisSupervisionKey sisSupervisionKey = new SisSupervisionKey();
        sisSupervisionKey.setSsId(ssId);
        sisSupervisionKey.setSsvWeek(sisSupervision.getSsvWeek());

        if (null != sisSupervisionMapper.selectByPrimaryKey(sisSupervisionKey))
            throw new InvalidPermissionException("Supervision exist: " + ssId + ", " + sisSupervision.getSsvWeek());

        //valid sisSchedule
        SisSchedule sisSchedule = Optional
            .ofNullable(sisScheduleMapper.selectByPrimaryKey(ssId))
            .orElseThrow(() -> new IncorrectParameterException("No ssId: " + ssId));

        //valid suspension
        Integer ssvWeek = sisSupervision.getSsvWeek();
        boolean isSuspend = sisSchedule.getSsSuspensionList()
            .stream()
            .anyMatch(week -> week.equals(ssvWeek));
        if (isSuspend)
            throw new InvalidPermissionException(String.format(
                "Schedule %d week %d is in the suspension list",
                ssId, ssvWeek));

        //valid permission
        SisCourse sisCourse =
            sisCourseMapper.selectByPrimaryKey(sisSchedule.getScId());
        if (!sisUser.getSuId().equals(sisCourse.getSuId())) {
            SisMonitorTransKey sisMonitorTransKey = new SisMonitorTransKey();
            sisMonitorTransKey.setSmtWeek(sisSupervision.getSsvWeek());
            sisMonitorTransKey.setSsId(ssId);

            Optional
                .ofNullable(sisMonitorTransMapper.selectByPrimaryKey(sisMonitorTransKey))
                .filter(sisMonitorTrans ->
                    sisMonitorTrans.getSmtStatus().equals(SisMonitorTrans.SmtStatus.AGREE.ordinal()) &&
                        sisUser.getSuId().equals(sisMonitorTrans.getSuId()))
                .orElseThrow(() ->
                    new InvalidPermissionException("No permission: " + sisSchedule.getSsId()));
        }

        //judge time
        if (!JudgeTimeUtil.isCourseTime(sisSchedule,
            sisSupervision.getSsvWeek(),
            currentDateTime)) {
            return new VoidOperationResponse(false, "Incorrect time");
        }

        if (sisSupervision.getSsvActualNum() > sisCourse.getScActSize())
            sisSupervision.setSsvActualNum(sisCourse.getScActSize());
        sisSupervision.setSsId(ssId);
        sisSupervisionMapper.insertSelective(sisSupervision);
        log.info("User " + sisUser.getSuId() + " insert supervision: ssId " + ssId + " week " + ssvWeek);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public List<SisMonitorTrans> getTransCourses(@NotNull SisUser sisUser,
                                                 Integer smtStatus) {
        SisMonitorTransExample sisMonitorTransExample =
            new SisMonitorTransExample();
        sisMonitorTransExample.createCriteria()
            .andSuIdEqualTo(sisUser.getSuId())
            .andSmtStatusEqualTo(smtStatus);

        List<SisMonitorTrans> sisMonitorTransList = sisMonitorTransMapper
            .selectByExample(sisMonitorTransExample);
        if (SisMonitorTrans.SmtStatus.AGREE.ordinal() == smtStatus && !sisMonitorTransList.isEmpty()) {
            SisSupervisionExample sisSupervisionExample =
                new SisSupervisionExample();
            sisMonitorTransList.forEach(t -> sisMonitorTransExample.or()
                .andSsIdEqualTo(t.getSsId())
                .andSmtWeekEqualTo(t.getSmtWeek()));
            List<SisSupervision> sisSupervisionList =
                sisSupervisionMapper.selectByExample(sisSupervisionExample);

            sisMonitorTransList.forEach(t -> t.setSisSupervision(sisSupervisionList.stream()
                .filter(s -> s.getSsId().equals(t.getSsId()) && s.getSsvWeek().equals(t.getSmtWeek()))
                .findAny()
                .orElse(null)));
        }

        joinMonitorTrans(sisMonitorTransList);

        return sisMonitorTransList;
    }

    @Transactional
    public VoidOperationResponse applyForTransfer(@NotNull SisUser sisUser,
                                                  @NotNull Integer ssId,
                                                  @NotNull SisMonitorTrans sisMonitorTrans) throws InvalidPermissionException, IncorrectParameterException {
        SisSchedule sisSchedule = Optional
            .ofNullable(sisScheduleMapper.selectByPrimaryKey(ssId))
            .orElseThrow(() ->
                new IncorrectParameterException("Incorrect ssId" + ssId));
        Integer smtWeek = sisMonitorTrans.getSmtWeek();
        boolean isSuspend = sisSchedule.getSsSuspensionList()
            .stream()
            .anyMatch(week -> week.equals(smtWeek));
        if (isSuspend)
            throw new InvalidPermissionException(String.format(
                "Schedule %d week %d is in the suspension list",
                ssId, smtWeek));

        //check exist
        SisMonitorTransKey sisMonitorTransKey = new SisMonitorTransKey();
        sisMonitorTransKey.setSsId(ssId);
        sisMonitorTransKey.setSmtWeek(sisMonitorTrans.getSmtWeek());
        SisMonitorTrans stdSisMonitorTrans =
            sisMonitorTransMapper.selectByPrimaryKey(sisMonitorTransKey);
        if (null != stdSisMonitorTrans)
            throw new InvalidPermissionException("MonitorTrans exist: " + new JSONObject(sisMonitorTransKey).toString());

        //check permission
        SisCourse sisCourse =
            sisCourseMapper.selectByPrimaryKey(sisSchedule.getScId());
        if (null == sisCourse ||
            null == sisCourse.getSuId() ||
            !sisCourse.getSuId().equals(sisUser.getSuId()))
            throw new InvalidPermissionException("Invalid Permission: ssId " + sisSchedule.getSsId());

        sisMonitorTrans.setSmtStatus(SisMonitorTrans.SmtStatus.UNTREATED.ordinal());
        sisMonitorTrans.setSuId(sisMonitorTrans.getSuId());
        sisMonitorTrans.setSsId(ssId);

        sisMonitorTransMapper.insert(sisMonitorTrans);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    @Transactional
    public VoidOperationResponse modifyTransfer(@NotNull SisUser sisUser,
                                                @NotNull Integer ssId,
                                                @NotNull SisMonitorTrans sisMonitorTrans) throws IncorrectParameterException, InvalidPermissionException {
        if (null == sisMonitorTrans.getSmtWeek())
            throw new IncorrectParameterException("Incorrect smtWeek: " + sisMonitorTrans.getSmtWeek());
        if (null == sisMonitorTrans.getSmtStatus())
            throw new IncorrectParameterException("Incorrect smtStatus: " + sisMonitorTrans.getSmtStatus());
        SisMonitorTransKey sisMonitorTransKey = new SisMonitorTransKey();
        sisMonitorTransKey.setSsId(ssId);
        sisMonitorTransKey.setSmtWeek(sisMonitorTrans.getSmtWeek());
        SisMonitorTrans stdSisMonitorTrans = Optional
            .ofNullable(sisMonitorTransMapper.selectByPrimaryKey(sisMonitorTransKey))
            .orElseThrow(() ->
                new IncorrectParameterException("Incorrect sisMonitorTrans: " + new JSONObject(sisMonitorTransKey).toString()));

        if (!stdSisMonitorTrans.getSuId().equals(sisUser.getSuId()))
            throw new InvalidPermissionException(
                "Invalid Permission: sisMonitorTrans " + new JSONObject(sisMonitorTransKey).toString());

        stdSisMonitorTrans.setSmtStatus(sisMonitorTrans.getSmtStatus());

        sisMonitorTransMapper.updateByPrimaryKey(stdSisMonitorTrans);
        log.info(String.format("success modify transfer: [%s,%s] , res %s",
            sisMonitorTrans.getSsId(),
            sisMonitorTrans.getSmtWeek(),
            sisMonitorTrans.getSmtStatus()));
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public PageInfo<SisUser> getMonitors(@NonNull Integer page,
                                         @NonNull Integer pageSize,
                                         @Nullable String suId,
                                         @Nullable String suName,
                                         @Nullable Boolean ordByLackNum) throws IncorrectParameterException {
        if (null == page)
            throw new IncorrectParameterException("Incorrect page: " + null);
        if (page < 1)
            throw new IncorrectParameterException("Incorrect page: " + page +
                " (must equal or bigger than 1)");
        else if (pageSize <= 0 || pageSize > 500)
            throw new IncorrectParameterException(
                "pageSize must between [1, 500]");

        SisUserExample sisUserExample = new SisUserExample();
        SisUserExample.Criteria criteria = sisUserExample.createCriteria();
        criteria.andSuAuthoritiesStrLike("%MONITOR%");

        if (null != suId) criteria.andSuIdLike("%" + suId + "%");
        if (null != suName)
            criteria.andSuNameLike(CourseService.getFuzzySearch(suName));
        if (null != ordByLackNum && ordByLackNum) {
            sisUserExample.setOrderByClause("sui_lack_num desc");
            sisUserExample.setOrdByLackNum(true);
        }

        PageHelper.startPage(page, pageSize);
        List<SisUser> sisUserList =
            sisUserMapper.selectByExample(sisUserExample);
        PageInfo<SisUser> pageInfo = new PageInfo<>(sisUserList);

        List<String> suIdList = sisUserList.stream()
            .map(SisUser::getSuId)
            .distinct()
            .collect(Collectors.toList());

        if (suIdList.isEmpty()) return pageInfo;

        SisUserInfoExample sisUserInfoExample = new SisUserInfoExample();
        sisUserInfoExample.createCriteria().andSuIdIn(suIdList);
        List<SisUserInfo> sisUserInfoList =
            sisUserInfoMapper.selectByExample(sisUserInfoExample);

        pageInfo.getList().forEach(u -> {
            u.setSuPassword(null);
            u.setSuiLackNum(sisUserInfoList.stream()
                .filter(sisUserInfo -> sisUserInfo.getSuId().equals(u.getSuId()))
                .findAny()
                .map(SisUserInfo::getSuiLackNum)
                .orElse(0));
        });
        return pageInfo;
    }

    public VoidOperationResponse grantMonitor(String suId) throws IncorrectParameterException {
        SisUser sisUser = sisUserMapper.selectByPrimaryKey(suId);
        if (null == sisUser)
            throw new IncorrectParameterException("Incorrect suId: " + suId);

        GrantedAuthority monitorAuth = new SimpleGrantedAuthority("MONITOR");
        List<GrantedAuthority> authList = sisUser.getSuAuthorities();
        if (!authList.contains(monitorAuth))
            authList.add(monitorAuth);

        SisUser updatedSisUser = new SisUser();
        updatedSisUser.setSuId(sisUser.getSuId());
        updatedSisUser.setSuAuthorities(authList);

        sisUserMapper.updateByPrimaryKeySelective(updatedSisUser);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public VoidOperationResponse revokeMonitor(String suId) throws IncorrectParameterException {
        SisUser sisUser = sisUserMapper.selectByPrimaryKey(suId);
        if (null == sisUser)
            throw new IncorrectParameterException("Incorrect suId: " + suId);

        GrantedAuthority monitorAuth = new SimpleGrantedAuthority("MONITOR");
        List<GrantedAuthority> authList = sisUser.getSuAuthorities();
        authList.remove(monitorAuth);

        SisUser updatedSisUser = new SisUser();
        updatedSisUser.setSuId(sisUser.getSuId());
        updatedSisUser.setSuAuthorities(authList);

        sisUserMapper.updateByPrimaryKeySelective(updatedSisUser);
        return VoidSuccessOperationResponse.SUCCESS;
    }

    public List<SisMonitorTrans> getMyTransCourses(SisUser sisUser) {
        SisCourseExample sisCourseExample = new SisCourseExample();
        sisCourseExample.createCriteria().andSuIdEqualTo(sisUser.getSuId());
        List<SisCourse> sisCourseList =
            sisCourseMapper.selectByExample(sisCourseExample);
        if (sisCourseList.isEmpty()) return new ArrayList<>();
        SisScheduleExample sisScheduleExample = new SisScheduleExample();
        sisScheduleExample.createCriteria()
            .andScIdIn(sisCourseList.stream()
                .map(SisCourse::getScId)
                .collect(Collectors.toList()));
        List<SisSchedule> sisScheduleList =
            sisScheduleMapper.selectByExample(sisScheduleExample);

        SisMonitorTransExample sisMonitorTransExample =
            new SisMonitorTransExample();
        sisMonitorTransExample.createCriteria()
            .andSsIdIn(sisScheduleList.stream()
                .map(SisSchedule::getSsId)
                .collect(Collectors.toList()));

        List<SisMonitorTrans> sisMonitorTransList = sisMonitorTransMapper
            .selectByExample(sisMonitorTransExample);
        joinMonitorTrans(sisMonitorTransList);

        return sisMonitorTransList;
    }

    private void joinMonitorTrans(List<SisMonitorTrans> sisMonitorTransList) {
        sisMonitorTransList
            .forEach(sisMonitorTrans -> {
                //get schedule
                SisSchedule sisSchedule =
                    sisScheduleMapper.selectByPrimaryKey(sisMonitorTrans.getSsId());

                //get course
                SisCourse sisCourse =
                    sisCourseMapper.selectByPrimaryKey(sisSchedule.getScId());

                SisJoinCourseExample sisJoinCourseExample =
                    new SisJoinCourseExample();
                sisJoinCourseExample.createCriteria()
                    .andJoinCourseTypeEqualTo(SisJoinCourse.JoinCourseType.TEACHING.ordinal())
                    .andScIdEqualTo(sisSchedule.getScId());
                List<SisJoinCourse> sisJoinCourseList =
                    sisJoinCourseMapper.selectByExample(sisJoinCourseExample);
                sisCourse.setSisJoinCourseList(sisJoinCourseList);

                SisUserExample sisUserExample = new SisUserExample();
                sisUserExample.createCriteria().andSuIdIn(sisJoinCourseList.stream()
                    .map(SisJoinCourse::getSuId)
                    .distinct()
                    .collect(Collectors.toList()));
                List<SisUser> sisUserList =
                    sisUserMapper.selectByExample(sisUserExample);

                sisJoinCourseList.forEach(j -> j.setSisUser(sisUserList.stream()
                    .filter(u -> u.getSuId().equals(j.getSuId()))
                    .peek(u -> u.setSuPassword(null))
                    .findAny()
                    .orElse(null)));


                //join monitor
                if (null != sisCourse.getSuId())
                    sisCourse.setMonitor(sisUserMapper.selectByPrimaryKey(sisCourse.getSuId()));

                //join course
                sisSchedule.setSisCourse(sisCourse);

                sisMonitorTrans.setSisSchedule(sisSchedule);
            });
    }
}

