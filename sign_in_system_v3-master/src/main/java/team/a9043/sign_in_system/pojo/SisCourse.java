package team.a9043.sign_in_system.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SisCourse {
    private String scId;

    private Integer scActSize;

    private BigDecimal scAttRate;

    private Integer scGrade;

    private Integer scMaxSize;

    private String scName;

    @NotNull
    private Boolean scNeedMonitor;

    private String suId;

    // extend
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisUser monitor;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SisSchedule> sisScheduleList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SisJoinCourse> sisJoinCourseList;
}