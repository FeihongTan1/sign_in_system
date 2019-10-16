package team.a9043.sign_in_system.controller;

import com.github.pagehelper.PageInfo;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.WxServerException;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.UserService;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.service_pojo.TokenResult;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;

import javax.annotation.Resource;
import java.util.concurrent.CompletableFuture;

/**
 * @author a9043
 */
@RestController
public class UserController {
    @Resource
    private UserService userService;

    @ApiOperation(value = "获取token", notes = "根据code获取token")
    @RequestMapping(value = "/tokens/{code}",
        method = {RequestMethod.GET, RequestMethod.POST})
    public DeferredResult<OperationResponse<TokenResult>> getTokens(@PathVariable
                                                                    @ApiParam(value =
                                                                        "微信获取code") String code) throws WxServerException {
        DeferredResult<OperationResponse<TokenResult>> deferredResult =
            new DeferredResult<>();
        CompletableFuture
            .supplyAsync(() -> userService.getTokensByCode(code))
            .whenComplete((res, err) -> {
                if (null != err) deferredResult.setErrorResult(err);
                deferredResult.setResult(res);
            });
        return deferredResult;
    }


    @ApiOperation(value = "初次修改微信绑定", notes = "根据code修改微信绑定")
    @PutMapping(value = "/users/{suId}")
    public DeferredResult<OperationResponse<String>> modifyBindUser(@TokenUser @ApiIgnore SisUser sisUser,
                                                                    @PathVariable @ApiParam(
                                                                        "用户名") String suId,
                                                                    @RequestParam @ApiParam(
                                                                        "微信code") String code) throws WxServerException, IncorrectParameterException {
        if (!sisUser.getSuId().equals(suId))
            throw new IncorrectParameterException("Incorrect suId: " + suId);
        DeferredResult<OperationResponse<String>> deferredResult =
            new DeferredResult<>();
        CompletableFuture
            .supplyAsync(() -> userService.modifyBindUser(sisUser, code))
            .whenComplete((res, err) -> {
                if (null != err) deferredResult.setErrorResult(err);
                deferredResult.setResult(res);
            });
        return deferredResult;
    }

    @GetMapping("/students")
    @ApiOperation("模糊搜索学生")
    public PageInfo<SisUser> getStudents(@RequestParam @ApiParam("页数") Integer page,
                                         @RequestParam @ApiParam("页大小") Integer pageSize,
                                         @RequestParam(required = false)
                                         @ApiParam("用户Id模糊") String suId,
                                         @RequestParam(required = false)
                                         @ApiParam("用户名字模糊") String suName,
                                         @RequestParam(required = false)
                                         @ApiParam("缺勤数排序")
                                             Boolean orderByCozLackNum) throws IncorrectParameterException {
        return userService.getStudents(page, pageSize, suId, suName,
            orderByCozLackNum);
    }

    @GetMapping("/teachers")
    @ApiOperation("模糊搜索老师")
    public PageInfo<SisUser> getTeacher(@RequestParam @ApiParam("页数") Integer page,
                                         @RequestParam @ApiParam("页大小") Integer pageSize,
                                         @RequestParam(required = false)
                                         @ApiParam("用户Id模糊") String suId,
                                         @RequestParam(required = false)
                                         @ApiParam("用户名字模糊") String suName) throws IncorrectParameterException {
        return userService.getTeachers(page, pageSize, suId, suName);
    }

    @PostMapping("/users/{suId}")
    @ApiOperation("修改密码")
    public VoidOperationResponse modifyPassword(@PathVariable @ApiParam(
        "用户Id") String suId,
                                                @RequestParam @ApiParam(
                                                    "用户旧密码") String oldPassword,
                                                @RequestParam @ApiParam(
                                                    "用户新密码") String newPassword) throws IncorrectParameterException {
        return userService.modifyPassword(suId, oldPassword, newPassword);
    }


    @DeleteMapping("/users/{suId}")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public VoidOperationResponse deleteUser(@PathVariable String suId) throws IncorrectParameterException {
        return userService.deleteUser(suId);
    }
}
