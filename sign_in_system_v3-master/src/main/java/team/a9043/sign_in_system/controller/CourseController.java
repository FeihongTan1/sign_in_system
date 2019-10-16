package team.a9043.sign_in_system.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.json.JSONArray;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.exception.UnknownServerError;
import team.a9043.sign_in_system.pojo.SisCourse;
import team.a9043.sign_in_system.pojo.SisDepartment;
import team.a9043.sign_in_system.pojo.SisJoinCourse;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.CourseService;
import team.a9043.sign_in_system.service.MonitorService;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.service_pojo.Week;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * @author a9043
 */
@RestController
public class CourseController {
    @Resource
    private CourseService courseService;
    @Resource
    private MonitorService monitorService;

    @GetMapping("/week")
    @ApiOperation(value = "获得当前周和服务器时间")
    public Week getWeek() throws InvalidTimeParameterException {
        LocalDateTime localDateTime = LocalDateTime.now();
        return courseService.getWeek(localDateTime);
    }


    @GetMapping("/departments")
    @ApiOperation(value = "获得学院", notes = "根据sdName获取课程")
    public List<SisDepartment> getDepartments(@RequestParam @ApiParam(value =
        "课程名字模糊") String sdName) {
        return courseService.getDepartments(sdName);
    }

    /**
     * 获得课程
     *
     * @param sisUser     用户
     * @param needMonitor null: 忽略
     *                    true: 开启督导课程
     *                    false: 关闭督导课程
     * @param hasMonitor  null: 忽略
     *                    true： 已有督导员
     *                    false： 无督导员
     * @param page        administrator 分页页数
     * @param getType     API 类型 （student, monitor, administrator）
     * @return json
     * @throws IncorrectParameterException 参数非法
     * @throws InvalidPermissionException  权限非法
     */
    @GetMapping("/courses")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','STUDENT','TEACHER'," +
        "'MONITOR')")
    @ApiOperation(value = "获得课程",
        notes = "根据getType获取课程",
        produces = "application/json")
    public DeferredResult<PageInfo<SisCourse>> getCourses(@TokenUser @ApiIgnore SisUser sisUser,
                                                          @RequestParam(required = false) @ApiParam(value = "分页filter") Integer page,
                                                          @RequestParam(required = false) @ApiParam(value = "分页大小filter") Integer pageSize,
                                                          @RequestParam(required = false) @ApiParam(value = "是否需要督导filter,若该参数为null则忽略hasMonitor") Boolean needMonitor,
                                                          @RequestParam(required = false) @ApiParam(value = "是否已有督导员filter") Boolean hasMonitor,
                                                          @RequestParam(required = false) @ApiParam(value = "学院Id") Integer sdId,
                                                          @RequestParam(required = false) @ApiParam(value = "开课年级") Integer scGrade,
                                                          @RequestParam(required = false) @ApiParam(value = "课程序号模糊") String scId,
                                                          @RequestParam(required = false) @ApiParam(value = "课程名字模糊") String scName,
                                                          @RequestParam(required = false) @ApiParam(value = "特别指定督导人学号") String suId,
                                                          @RequestParam(required = false) @ApiParam(value = "排序列") String orderCol,
                                                          @RequestParam(required = false) @ApiParam(value = "排序") String order,
                                                          @RequestParam(required = false) @ApiParam(value = "role=ADMINISTRATOR&&getType=teacher时候必选") String tchSuId,
                                                          @RequestParam @ApiParam(value = "获得方式", allowableValues = "student,monitor," + "administrator,teacher") String getType) throws IncorrectParameterException, InvalidPermissionException {
        DeferredResult<PageInfo<SisCourse>> deferredResult =
            new DeferredResult<>();

        CompletableFuture
            .supplyAsync(() -> {
                switch (getType) {
                    case "administrator": {
                        try {
                            return getCoursesAdm(sisUser, page, pageSize,
                                needMonitor,
                                hasMonitor, sdId, scGrade, scId, scName, orderCol, order);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                            throw new UnknownServerError(e.getMessage());
                        }
                    }
                    case "monitor": {
                        try {
                            return getCoursesMonitor(sisUser, page, pageSize,
                                needMonitor,
                                hasMonitor, suId);
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                            throw new UnknownServerError(e.getMessage());
                        }
                    }
                    case "teacher": {
                        if (!sisUser.getSuAuthoritiesStr().contains("TEACHER")) {
                            if (sisUser.getSuAuthoritiesStr().contains("ADMINISTRATOR") &&
                                null != tchSuId) {
                                SisUser tUser = new SisUser();
                                tUser.setSuId(tchSuId);
                                return courseService.getTeacherCourses(tUser);
                            }
                            throw new InvalidPermissionException(
                                "Invalid permission:" + getType);
                        }
                        return courseService.getTeacherCourses(sisUser);
                    }
                    case "student": {
                        if (!sisUser.getSuAuthoritiesStr().contains("STUDENT")) {
                            throw new InvalidPermissionException(
                                "Invalid permission:" + getType);
                        }
                        return courseService.getStudentCourses(sisUser);
                    }
                    default:
                        throw new IncorrectParameterException("Incorrect " +
                            "getType:" + getType);
                }
            })
            .whenComplete((res, err) -> {
                if (null != err) deferredResult.setErrorResult(err.getCause());
                deferredResult.setResult(res);
            });
        return deferredResult;
    }

    /**
     * 修改是否需要督导
     *
     * @param sisCourse     课程json
     * @param bindingResult 检验结果
     * @param scId          课程id
     * @return json
     * @throws IncorrectParameterException 参数非法
     */
    @PutMapping("/courses/{scId}/sc-need-monitor")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @ApiOperation(value = "修改督导",
        notes = "根据scId修改督导,SisCourse -> {\n  scNeedMonitor: boolean,\n  " +
            "monitor: SisUser - {suId: String}\n}",
        produces = "application/json")
    public VoidOperationResponse modifyScNeedMonitor(@RequestBody @Validated SisCourse sisCourse,
                                                     @ApiIgnore BindingResult bindingResult,
                                                     @PathVariable
                                                     @ApiParam(value = "课程序号") String scId) throws
        IncorrectParameterException {
        if (bindingResult.hasErrors()) {
            throw new IncorrectParameterException(new JSONArray(bindingResult.getAllErrors()).toString());
        }
        sisCourse.setScId(scId);
        return courseService.modifyScNeedMonitor(sisCourse);
    }

    @PutMapping("/courses/sc-need-monitor")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    @ApiOperation(value = "批量修改督导",
        notes = "批量修改督导根据搜索条件")
    public VoidOperationResponse batchSetNeedMonitor(@RequestParam
                                                     @ApiParam(value =
                                                         "修改督导状态") Boolean monitorStatus,
                                                     @RequestBody(required =
                                                         false) List<String> scIdList,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "是否需要督导filter,若该参数为null则忽略hasMonitor"
                                                     ) Boolean
                                                         needMonitor,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "是否已有督导员filter") Boolean hasMonitor,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "学院Id") Integer sdId,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "开课年级") Integer scGrade,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "课程序号模糊") String scId,
                                                     @RequestParam(required =
                                                         false) @ApiParam(value = "课程名字模糊") String scName) {
        if (null != scIdList) {
            return courseService.batchSetNeedMonitor(monitorStatus,
                scIdList);
        }
        return courseService.batchSetNeedMonitor(monitorStatus, needMonitor,
            hasMonitor, sdId, scGrade, scId, scName);
    }

    @GetMapping("/courses/{scId}/departments")
    public List<SisDepartment> getCourseDepartments(@PathVariable String
                                                        scId) {
        return courseService.getCourseDepartments(scId);
    }

    @GetMapping("/courses/{scId}/joinCourses")
    public List<SisJoinCourse> getJoinCourseStudents(@PathVariable String
                                                         scId) {
        return courseService.getJoinCourseStudents(scId);
    }

    // prvate
    private PageInfo<SisCourse> getCoursesAdm(SisUser sisUser,
                                              Integer page,
                                              Integer pageSize,
                                              Boolean needMonitor,
                                              Boolean hasMonitor,
                                              Integer sdId,
                                              Integer scGrade,
                                              String scId,
                                              String scName,
                                              String orderCol,
                                              String order) throws ExecutionException, InterruptedException {
        if (!sisUser.getSuAuthoritiesStr().contains(
            "ADMINISTRATOR")) {
            throw new InvalidPermissionException(
                "Invalid permission: administrator");
        }
        return courseService.getCourses(page,
            pageSize, needMonitor,
            hasMonitor,
            sdId, scGrade, scId, scName, orderCol, order);
    }

    private PageInfo<SisCourse> getCoursesMonitor(SisUser sisUser,
                                                  Integer page,
                                                  Integer pageSize,
                                                  Boolean needMonitor,
                                                  Boolean hasMonitor,
                                                  String suId) throws ExecutionException, InterruptedException {
        if (null != suId) {
            if (!sisUser.getSuAuthoritiesStr()
                .contains("ADMINISTRATOR")) {
                throw new InvalidPermissionException(
                    "Invalid permission:" + suId);
            }
            SisUser sisUser1 = new SisUser();
            sisUser1.setSuId(suId);
            return monitorService.getCourses(sisUser1);
        }
        if (!sisUser.getSuAuthoritiesStr().contains("MONITOR")) {
            throw new InvalidPermissionException(
                "Invalid permission:" + needMonitor + "," + hasMonitor);
        }
        if (null != needMonitor && null != hasMonitor) {
            if (needMonitor && !hasMonitor) {
                return courseService.getCourses(page,
                    pageSize,
                    true,
                    false,
                    null, null, null, null, null, null);
            }
            throw new InvalidPermissionException(
                "Invalid permission: monitor");
        } else if (null == needMonitor && null == hasMonitor) {
            return monitorService.getCourses(sisUser);
        } else
            throw new InvalidPermissionException(
                "Invalid permission:" + needMonitor + "," + hasMonitor);
    }
}
