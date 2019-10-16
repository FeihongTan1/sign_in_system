package team.a9043.sign_in_system.convertor;

import org.json.JSONArray;
import org.json.JSONException;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.*;

/**
 * org.json.JSONArray HttpMessageConverter
 *
 * @author a9043
 */
@Component
public class JsonArrayHttpMessageConverter extends AbstractHttpMessageConverter<JSONArray> {

    public JsonArrayHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN);
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        return JSONArray.class.equals(clazz);
    }

    @Override
    protected JSONArray readInternal(Class<? extends JSONArray> clazz, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpInputMessage.getBody()));
        bufferedReader.lines().forEachOrdered(stringBuilder::append);
        try {
            return new JSONArray(stringBuilder.toString());
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException(e.getMessage());
        }
    }

    @Override
    protected void writeInternal(JSONArray objects, HttpOutputMessage outputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStreamWriter outputStreamWriter =
            new OutputStreamWriter(outputMessage.getBody());
        objects.write(outputStreamWriter).flush();
    }
}
