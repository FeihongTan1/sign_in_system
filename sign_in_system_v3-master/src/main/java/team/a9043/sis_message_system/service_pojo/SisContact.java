package team.a9043.sis_message_system.service_pojo;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

public class SisContact implements Serializable {
    private static final long serialVersionUID = 113586698392218838L;
    private Integer sctId;

    @NotBlank
    private String sctName;

    @NotBlank
    private String sctContact;

    @NotBlank
    private String sctContent;

    public Integer getSctId() {
        return sctId;
    }

    public void setSctId(Integer sctId) {
        this.sctId = sctId;
    }

    public String getSctName() {
        return sctName;
    }

    public void setSctName(String sctName) {
        this.sctName = sctName == null ? null : sctName.trim();
    }

    public String getSctContact() {
        return sctContact;
    }

    public void setSctContact(String sctContact) {
        this.sctContact = sctContact == null ? null : sctContact.trim();
    }

    public String getSctContent() {
        return sctContent;
    }

    public void setSctContent(String sctContent) {
        this.sctContent = sctContent == null ? null : sctContent.trim();
    }
}