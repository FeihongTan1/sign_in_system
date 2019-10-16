package team.a9043.sign_in_system.convertor;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * org.json.JSONObject HttpMessageConverter
 *
 * @author a9043
 */
@Component
public class JsonObjectHttpMessageConverter extends AbstractHttpMessageConverter<JSONObject> {

    public JsonObjectHttpMessageConverter() {
        super(MediaType.APPLICATION_JSON, MediaType.APPLICATION_JSON_UTF8, MediaType.TEXT_PLAIN);
    }

    @Override
    protected boolean supports(Class<?> aClass) {
        return JSONObject.class.equals(aClass);
    }

    @Override
    protected JSONObject readInternal(Class<? extends JSONObject> aClass, HttpInputMessage httpInputMessage) throws IOException, HttpMessageNotReadableException {
        StringBuilder stringBuilder = new StringBuilder();
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(httpInputMessage.getBody()));
        bufferedReader.lines().forEachOrdered(stringBuilder::append);
        try {
            return new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            throw new HttpMessageNotReadableException(e.getMessage());
        }
    }

    @Override
    protected void writeInternal(JSONObject jsonObject, HttpOutputMessage httpOutputMessage) throws IOException, HttpMessageNotWritableException {
        OutputStreamWriter outputStreamWriter =
            new OutputStreamWriter(httpOutputMessage.getBody());
        jsonObject.write(outputStreamWriter).flush();
    }
}
