package team.a9043.sis_message_system.service_pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class SignInMessage implements Serializable {
    private static final long serialVersionUID = -3084604938284789226L;

    private int ssId;
    private LocalDateTime localDateTime;

    public SignInMessage(int ssId, LocalDateTime localDateTime) {
        this.ssId = ssId;
        this.localDateTime = localDateTime;
    }
}
