package team.a9043.sign_in_system.convertor;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.stream.FileImageOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Base64;

@Slf4j
public class Byte2ImgSerializer extends JsonSerializer<byte[]> {
    @Override
    public void serialize(byte[] value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        String pathStr = "/home/sis-user/javaApps/sign_in_system/temp/imgs";
        File file = new File(pathStr);
        if (!file.exists() && !file.mkdirs()) {
            gen.writeString("");
            return;
        }

        String filename = String.valueOf(Arrays.hashCode(value));
        String filePath = String.format("%s/%s.png", pathStr, filename);
        String urlPath = String.format("/imgs/%s.png", filename);
        file = new File(filePath);
        if (!file.exists() && !file.createNewFile()) {
            gen.writeString("");
            return;
        }

        FileImageOutputStream fileImageOutputStream = new FileImageOutputStream(file);
        fileImageOutputStream.write(value, 0, value.length);
        fileImageOutputStream.close();
        log.info(urlPath);
        gen.writeString(urlPath);
    }
}
