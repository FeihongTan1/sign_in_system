package team.a9043.sign_in_system.service_pojo;

/**
 * @author a9043
 */
public class VoidOperationResponse extends OperationResponse<Void> {
    public VoidOperationResponse() {
    }

    public VoidOperationResponse(boolean success, String message) {
        super(success, message);
    }

    public VoidOperationResponse(boolean success, String message, Integer code) {
        super(success, message, code);
    }

    public void setData(Void data) {
    }

    public Void getData() {
        return null;
    }
}
