package team.a9043.sis_message_system.service_pojo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class FormId implements Serializable {
    private static final long serialVersionUID = 2147306485722861430L;

    public FormId(String formId, LocalDateTime localDateTime) {
        this.formId = formId;
        this.localDateTime = localDateTime;
    }

    private String formId;
    private LocalDateTime localDateTime;
}
