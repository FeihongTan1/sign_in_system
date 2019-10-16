package team.a9043.sign_in_system.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import team.a9043.sign_in_system.service_pojo.OperationResponse;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.UUID;

/**
 * @author a9043
 */
@Service
public class FileService {
    @Resource
    private ImportService importService;

    public OperationResponse<String> readStuInfo(MultipartFile multipartFile) throws IOException {
        String key =
            "SIS_Process_" + UUID.randomUUID().toString().replaceAll("-", "");
        importService.readStuInfo(key, multipartFile.getInputStream());
        OperationResponse<String> operationResponse = new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(key);
        operationResponse.setMessage("data => key");
        return operationResponse;
    }

    public OperationResponse<String> readCozInfo(MultipartFile multipartFile) throws IOException {
        String key =
            "SIS_Process_" + UUID.randomUUID().toString().replaceAll("-", "");
        importService.readCozInfo(key, multipartFile.getInputStream());
        OperationResponse<String> operationResponse = new OperationResponse<>();
        operationResponse.setSuccess(true);
        operationResponse.setData(key);
        operationResponse.setMessage("data => key");
        return operationResponse;
    }
}
