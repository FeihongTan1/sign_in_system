package team.a9043.sign_in_system.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class SisMonitorTrans extends SisMonitorTransKey {
    public enum SmtStatus {
        UNTREATED(), AGREE(), DISAGREE();

        public static Integer lowercase2Value(String value) throws String2ValueException {
            switch (value) {
                case "untreated":
                    return UNTREATED.ordinal();
                case "agree":
                    return AGREE.ordinal();
                case "disagree":
                    return DISAGREE.ordinal();
                default:
                    throw new String2ValueException("No enum: " + value);
            }
        }
    }

    @ApiModelProperty("转接人")
    @NotNull
    private String suId;

    @ApiModelProperty(value = "转接状态",
        allowableValues = "0(untreated), 1(agree), 2(disagree)")
    private Integer smtStatus;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisSchedule sisSchedule;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisSupervision sisSupervision;
}