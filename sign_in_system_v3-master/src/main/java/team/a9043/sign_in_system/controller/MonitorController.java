package team.a9043.sign_in_system.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.json.JSONArray;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.pojo.String2ValueException;
import team.a9043.sign_in_system.pojo.SisMonitorTrans;
import team.a9043.sign_in_system.pojo.SisSchedule;
import team.a9043.sign_in_system.pojo.SisSupervision;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.MonitorService;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;
import team.a9043.sign_in_system.util.judgetime.ScheduleParserException;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author a9043
 */
@RestController
public class MonitorController {
    @Resource
    private MonitorService monitorService;

    @GetMapping("/monitors")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @ApiOperation(value = "获得督导员", notes = "获得督导员")
    public PageInfo<SisUser> getMonitors(@RequestParam @ApiParam("页数") Integer page,
                                         @RequestParam @ApiParam("页数大小") Integer pageSize,
                                         @RequestParam(required = false) @ApiParam(
                                             "学号模糊") String suId,
                                         @RequestParam(required = false) @ApiParam(
                                             "姓名模糊") String suName,
                                         @RequestParam(required = false) @ApiParam(
                                             "是否按督导分排序") Boolean ordByLackNum) throws IncorrectParameterException {
        return monitorService.getMonitors(page, pageSize, suId, suName,
            ordByLackNum);
    }

    @PostMapping("/courses/{scId}/monitor")
    @PreAuthorize("hasAuthority('MONITOR')")
    @ApiOperation(value = "领取督导池", notes = "只可领取, 不可取消")
    public VoidOperationResponse drawMonitor(@TokenUser @ApiIgnore SisUser sisUser,
                                             @PathVariable @ApiParam(value =
                                                 "课程序号") String scId) throws IncorrectParameterException {

        return monitorService.drawMonitor(sisUser, scId);
    }

    @GetMapping("/courses/{scId}/supervisions")
    @PreAuthorize("hasAnyAuthority('MONITOR','ADMINISTRATOR','TEACHER')")
    @ApiOperation(value = "获取督导记录", notes = "根据课程序号获取督导记录")
    public List<SisSchedule> getSupervisions(@TokenUser @ApiIgnore SisUser sisUser,
                                             @PathVariable @ApiParam(value =
                                                 "课程序号") String scId) throws InvalidPermissionException, IncorrectParameterException {
        return monitorService.getSupervisions(sisUser, scId);
    }

    @SuppressWarnings({"ELValidationInJSP", "SpringElInspection"})
    @PostMapping(value = "/schedules/{ssId}/supervisions")
    @PreAuthorize("hasAuthority('MONITOR') &&" +
        "authentication.sisUser.type.equals('code')")
    @ApiOperation(value = "插入督导记录",
        notes = "SisSupervision{ssvWeek, ssvActualNum, ssvMobileNum, " +
            "ssvSleepNum, ssvRecInfo}",
        produces = "application/json")
    public VoidOperationResponse insertSupervision(@TokenUser @ApiIgnore SisUser sisUser,
                                                   @PathVariable
                                                   @ApiParam(value = "排课号") Integer ssId,
                                                   @RequestBody @Validated SisSupervision sisSupervision,
                                                   @ApiIgnore BindingResult bindingResult) throws IncorrectParameterException, ScheduleParserException, InvalidPermissionException, InvalidTimeParameterException {

        if (bindingResult.hasErrors()) {
            throw new IncorrectParameterException(new JSONArray(bindingResult.getAllErrors()).toString());
        }

        LocalDateTime localDateTime = LocalDateTime.now();
        return monitorService.insertSupervision(
            sisUser,
            ssId,
            sisSupervision,
            localDateTime);
    }

    @PostMapping(value = "/schedules/{ssId}/monitor-trans")
    @PreAuthorize("hasAuthority('MONITOR')")
    @ApiOperation(value = "申请转接",
        notes = "SisMonitorTrans{smtWeek, suId}",
        produces = "application/json")
    public VoidOperationResponse applyForTransfer(@TokenUser @ApiIgnore SisUser sisUser,
                                                  @PathVariable @ApiParam(
                                                      "排课") Integer ssId,
                                                  @RequestBody @Validated SisMonitorTrans sisMonitorTrans,
                                                  @ApiIgnore BindingResult bindingResult) throws IncorrectParameterException, InvalidPermissionException {
        if (bindingResult.hasErrors()) {
            throw new IncorrectParameterException(new JSONArray(bindingResult.getAllErrors()).toString());
        }
        return monitorService.applyForTransfer(sisUser, ssId, sisMonitorTrans);
    }

    @PutMapping("/schedules/{ssId}/monitor-trans")
    @PreAuthorize("hasAuthority('MONITOR')")
    @ApiOperation(value = "接受或拒绝转接",
        notes = "SisMonitorTrans{smtStatus, smtWeek}",
        produces = "application/json")
    public VoidOperationResponse modifyTransfer(@TokenUser @ApiIgnore SisUser sisUser,
                                                @PathVariable @ApiParam("排课") Integer ssId,
                                                @RequestBody SisMonitorTrans sisMonitorTrans) throws IncorrectParameterException, InvalidPermissionException {
        return monitorService.modifyTransfer(sisUser, ssId, sisMonitorTrans);
    }

    @GetMapping("/schedules/monitor-trans")
    @PreAuthorize("hasAuthority('MONITOR')")
    @ApiOperation(value = "获取转接课程", notes = "根据smtStatus获取转接课程",
        produces =
            "application/json")
    public List<SisMonitorTrans> getTransCourses(@TokenUser @ApiIgnore SisUser sisUser,
                                                 @RequestParam
                                                 @ApiParam(value = "获得方式",
                                                     allowableValues =
                                                         "untreated,agree," +
                                                             "disagree,mine")
                                                     String smtStatus) throws String2ValueException {
        if ("mine".equals(smtStatus)) {
            return monitorService.getMyTransCourses(sisUser);
        }
        return monitorService.getTransCourses(sisUser,
            SisMonitorTrans.SmtStatus.lowercase2Value(smtStatus));
    }

    @PostMapping("/monitors/{suId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @ApiOperation("授予督导权")
    public VoidOperationResponse grantMonitor(@PathVariable @ApiParam("用户Id") String suId) throws IncorrectParameterException {
        return monitorService.grantMonitor(suId);
    }

    @DeleteMapping("/monitors/{suId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @ApiOperation("撤回")
    public VoidOperationResponse revokeMonitor(@PathVariable @ApiParam("用户Id") String suId) throws IncorrectParameterException {
        return monitorService.revokeMonitor(suId);
    }
}
