package team.a9043.sign_in_system.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.tokenuser.TokenUser;
import team.a9043.sign_in_system.service.MessageService;

import javax.annotation.Resource;

@RestController
public class MessageController {
    @Resource
    private MessageService messageService;

    @GetMapping("/formIds")
    public void receiveFormId(@TokenUser @ApiIgnore SisUser sisUser,
                              @RequestParam String formIdStr) {
        messageService.receiveFormId(sisUser, formIdStr);
    }
}
