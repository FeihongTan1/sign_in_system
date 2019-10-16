package team.a9043.sign_in_system.controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.poi.ss.usermodel.Workbook;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.exception.IncorrectParameterException;
import team.a9043.sign_in_system.exception.InvalidPermissionException;
import team.a9043.sign_in_system.pojo.SisCourse;
import team.a9043.sign_in_system.pojo.SisSignIn;
import team.a9043.sign_in_system.pojo.SisSignInDetail;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.SignInService;
import team.a9043.sign_in_system.service_pojo.OperationResponse;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;
import team.a9043.sign_in_system.util.JwtUtil;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

/**
 * @author a9043
 */
@RestController
public class SignInController {
    private SecretKeySpec secretKey;
    @Resource
    private SignInService signInService;

    public SignInController(@Value("${location.secretKey}")
                                String secretKeyStr) {
        this.secretKey =
            new SecretKeySpec(Base64.getDecoder().decode(secretKeyStr), "AES");
    }

    @GetMapping("/users/{suId}/signIns")
    public List<SisSignInDetail> getSignIns(@PathVariable String suId) {
        return signInService.getStuSignIns(suId);
    }

    @GetMapping("/courses/{scId}/signIns")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','STUDENT','TEACHER')")
    @ApiOperation("获得签到以及历史")
    public SisCourse getSignIns(@TokenUser @ApiIgnore SisUser sisUser,
                                @PathVariable @ApiParam("课程") String scId,
                                @RequestParam
                                @ApiParam(value = "查询类型", allowableValues =
                                    "student, teacher, administrator")
                                    String queryType) throws IncorrectParameterException, InvalidPermissionException {
        switch (queryType) {
            case "teacher":
                if (!sisUser.getSuAuthoritiesStr().contains("TEACHER") && !sisUser.getSuAuthoritiesStr().contains("ADMINISTRATOR")) {
                    throw new InvalidPermissionException(
                        "Invalid permission: queryType " + queryType);
                }
                return signInService.getSignIns(scId);
            case "administrator":
                if (!sisUser.getSuAuthoritiesStr().contains("ADMINISTRATOR")) {
                    throw new InvalidPermissionException(
                        "Invalid permission: queryType " + queryType);
                }
                return signInService.getSignIns(scId);
            case "student":
                if (!sisUser.getSuAuthoritiesStr().contains("STUDENT")) {
                    throw new InvalidPermissionException(
                        "Invalid permission: queryType " + queryType);
                }
                return signInService.getSignIns(sisUser, scId);
            default:
                throw new IncorrectParameterException(
                    "Invalid permission: queryType " + queryType);
        }
    }

    @GetMapping("/courses/{scId}/signIns/export")
    @ApiOperation("导出签到以及历史")
    public void exportSignIns(@PathVariable @ApiParam("课程") String scId,
                              @RequestParam String accessToken,
                              HttpServletResponse httpServletResponse) throws IOException {
        try {
            Claims claims = JwtUtil.parseJwt(URLDecoder.decode(accessToken));
            String auth = claims.get("suAuthoritiesStr", String.class);
            if (!auth.contains("ADMINISTRATOR") && !auth.contains("TEACHER"))
                throw new InvalidPermissionException("Invalid Permission: " + auth);
        } catch (MalformedJwtException | SignatureException | ExpiredJwtException e) {
            throw new InvalidPermissionException("Invalid Permission: " + null);
        }
        httpServletResponse.setHeader("content-Type", "application/vnd.ms-excel");
        httpServletResponse.setHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode("signIns.xlsx", "utf-8"));
        Workbook workbook = signInService.exportSignIns(scId);
        workbook.write(httpServletResponse.getOutputStream());
        workbook.close();
    }

    @PostMapping("/schedules/{ssId}/signIns")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','TEACHER','MONITOR')")
    @ApiOperation("发起签到")
    public VoidOperationResponse createSignIn(@TokenUser @ApiIgnore SisUser sisUser,
                                              @PathVariable @ApiParam("排课") Integer ssId,
                                              @RequestPart(value = "picture", required = false) byte[] picBytes) throws InvalidTimeParameterException, InvalidPermissionException {
        LocalDateTime localDateTime = LocalDateTime.now();

        return signInService.createSignIn(sisUser, ssId, picBytes, localDateTime);
    }

    @GetMapping("/schedules/{ssId}/signIns/week/{week}")
    @ApiOperation("获得签到")
    public OperationResponse<SisSignIn> getSignIn(@PathVariable @ApiParam("排课") Integer ssId,
                                                  @PathVariable @ApiParam("签到周") Integer week) {
        return signInService.getSignIn(ssId, week);
    }

    @PostMapping("/schedules/{ssId}/signIns/doBackSignIn")
    @PreAuthorize("hasAnyAuthority('STUDENT') && authentication.sisUser.type" +
        ".equals('code')")
    @ApiOperation("学生备用签到")
    public VoidOperationResponse backSignIn(@TokenUser @ApiIgnore SisUser sisUser,
                                            @PathVariable @ApiParam("排课") Integer ssId,
                                            @RequestPart("picture") byte[] bytes) throws InvalidTimeParameterException {
        LocalDateTime localDateTime = LocalDateTime.now();
        return signInService.backSignIn(sisUser, ssId, bytes, localDateTime);
    }


    @SuppressWarnings({"ELValidationInJSP", "SpringElInspection"})
    @PostMapping("/schedules/{ssId}/signIns/doSignIn")
    @PreAuthorize("hasAnyAuthority('STUDENT') && authentication.sisUser.type" +
        ".equals('code')")
    @ApiOperation("学生签到")
    public VoidOperationResponse signIn(@TokenUser @ApiIgnore SisUser sisUser,
                                        @PathVariable @ApiParam("排课") Integer ssId,
                                        @RequestHeader("Access-Token")
                                        @ApiParam(value = "json的加密内容进行Base64编码",
                                            allowableValues = "{loc_lat: Double, " +
                                                "loc_long: Double}") String base64EncodeAESBytesStr) throws IncorrectParameterException, InvalidTimeParameterException, InvalidPermissionException {
        JSONObject locationJson;
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String locationString =
                new String(cipher.doFinal(Base64.getDecoder().decode(base64EncodeAESBytesStr)));
            locationJson =
                new JSONObject(locationString);
        } catch (JSONException | NoSuchAlgorithmException | IllegalBlockSizeException | BadPaddingException | NoSuchPaddingException | InvalidKeyException | IllegalArgumentException e) {
            throw new IncorrectParameterException(e.getMessage() + " " +
                "Base64Str: " + base64EncodeAESBytesStr);
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        return signInService.signIn(sisUser, ssId, localDateTime, locationJson);
    }

    @PutMapping("/signIns/{ssiId}")
    @PreAuthorize("hasAnyAuthority('ADMINISTRATOR','TEACHER','MONITOR')")
    public VoidOperationResponse modifySignIns(@TokenUser SisUser sisUser,
                                               @PathVariable Integer ssiId,
                                               @RequestBody List<SisSignInDetail> sisSignInDetailList) {
        return signInService.modifySignIns(sisUser, ssiId, sisSignInDetailList);
    }
}
