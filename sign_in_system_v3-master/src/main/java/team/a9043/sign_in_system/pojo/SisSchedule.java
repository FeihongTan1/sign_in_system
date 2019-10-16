package team.a9043.sign_in_system.pojo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import team.a9043.sign_in_system.service_pojo.SignInProcessing;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Getter
@Setter
public class SisSchedule {
    public enum SsFortnight {
        FULL(), ODD(), EVEN();

        public static SisSchedule.SsFortnight valueOf(int ordinal) throws IndexOutOfBoundsException {
            if (ordinal < 0 || ordinal >= values().length) {
                throw new IndexOutOfBoundsException("Invalid ordinal");
            }
            return values()[ordinal];
        }
    }

    public enum SsTerm {
        FIRST(), SECOND();

        public static SisSchedule.SsTerm toEnum(int value) {
            switch (value) {
                case 1:
                    return FIRST;
                case 2:
                    return SECOND;
                default:
                    return null;
            }
        }
    }

    public List<Integer> getSsSuspensionList() {
        return Arrays.stream(ssSuspension.split(","))
            .map(String::trim)
            .map(s -> {
                try {
                    return Integer.valueOf(s);
                } catch (NumberFormatException e) {
                    return null;
                }
            })
            .filter(Objects::nonNull)
            .filter(integer -> integer > 0)
            .collect(Collectors.toList());
    }

    public void setSsSuspensionList(List<Integer> ssSuspensionList) {
        this.ssSuspension = String.join(",",
            (String[]) ssSuspensionList
                .stream()
                .map(Object::toString)
                .toArray());
    }

    private Integer ssId;

    private Integer ssDayOfWeek;

    private Integer ssEndTime;

    private Integer ssEndWeek;

    private Integer ssFortnight;

    private Integer ssStartTime;

    private Integer ssStartWeek;

    private String ssYearEtTerm;

    private String scId;

    private String ssSuspension;

    private String ssRoom;

    private Integer slId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SisSupervision> sisSupervisionList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private SisCourse sisCourse;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SisSignIn> sisSignInList;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<SignInProcessing> sisProcessingList;
}