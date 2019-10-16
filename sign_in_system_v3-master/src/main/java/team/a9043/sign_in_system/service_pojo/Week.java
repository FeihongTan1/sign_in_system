package team.a9043.sign_in_system.service_pojo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author a9043
 */
@Getter
@Setter
@AllArgsConstructor
public class Week {
    private LocalDateTime serverTime;
    private int week;
}
