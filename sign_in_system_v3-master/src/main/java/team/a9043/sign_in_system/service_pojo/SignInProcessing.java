package team.a9043.sign_in_system.service_pojo;

import lombok.Getter;
import lombok.Setter;

/**
 * @author a9043
 */
@Getter
@Setter
public class SignInProcessing {
    private Integer ssId;
    private Integer week;

    public SignInProcessing(Integer ssId, Integer week) {
        this.ssId = ssId;
        this.week = week;
    }
}
