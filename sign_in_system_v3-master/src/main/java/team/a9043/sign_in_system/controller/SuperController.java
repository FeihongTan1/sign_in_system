package team.a9043.sign_in_system.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.SuperService;
import team.a9043.sign_in_system.service_pojo.VoidOperationResponse;

import javax.annotation.Resource;
import java.util.List;

@RestController
public class SuperController {
    @Resource
    private SuperService superService;

    @GetMapping("/administrators")
    @PreAuthorize("hasAuthority('SUPER_ADMINISTRATOR')")
    public List<SisUser> getAdmin() {
        return superService.getAdministrators();
    }

    @PostMapping("/administrators/{suId}/lock_grade/{grade}")
    public VoidOperationResponse modifyGrade(@PathVariable String suId,
                                             @PathVariable Integer grade) {
        return superService.modifyGrade(suId, grade);
    }

    @GetMapping("/lock_grade")
    @PreAuthorize("hasAuthority('ADMINISTRATOR')")
    public Integer getLockGrade(@TokenUser @ApiIgnore SisUser sisUser) {
        return superService.getLockGrade(sisUser.getSuId());
    }
}
