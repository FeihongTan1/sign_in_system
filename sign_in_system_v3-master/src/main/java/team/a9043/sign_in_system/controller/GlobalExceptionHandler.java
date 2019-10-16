package team.a9043.sign_in_system.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.POIXMLException;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.firewall.RequestRejectedException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import team.a9043.sign_in_system.exception.*;
import team.a9043.sign_in_system.pojo.String2ValueException;
import team.a9043.sign_in_system.util.judgetime.InvalidTimeParameterException;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@ControllerAdvice
@RestController
@Slf4j
public class GlobalExceptionHandler {
    private static final String errResStr =
        "{\"success\":false,\"error\":true,\"errType\":\"%s\"," +
            "\"message\":%s}";

    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleNotFound(Exception e,
                               HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.NOT_FOUND, e.getMessage()));
    }

    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public void handleUnSupportedMediaType(Exception e,
                                           HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage()));
    }

    @ExceptionHandler({
        AccessDeniedException.class,
        InvalidPermissionException.class
    })
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleForbidden(Exception e,
                                HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.FORBIDDEN, e.getMessage()));
    }

    @ExceptionHandler({
        MissingServletRequestParameterException.class,
        MissingPathVariableException.class,
        IncorrectParameterException.class,
        InvalidTimeParameterException.class,
        String2ValueException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestPartException.class,
        InvalidFormatException.class,
        POIXMLException.class,
        ServletRequestBindingException.class,
        RequestRejectedException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public void handleBadRequest(Exception e,
                                 HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.BAD_REQUEST, e.getMessage()));
    }

    @ExceptionHandler({
        HttpMediaTypeNotSupportedException.class
    })
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public void handleUnsupportedMediaType(Exception e,
                                           HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.UNSUPPORTED_MEDIA_TYPE, e.getMessage()));
    }

    @ExceptionHandler({
        WxServerException.class,
        UnknownServerError.class
    })
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleInternalServerError(Exception e,
                                          HttpServletResponse response) throws IOException {
        log.error(e.getMessage(), e);
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        response.getWriter().write(
            formatErr(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public void handleOther(Exception e,
                            HttpServletResponse response) throws IOException {
        response.setHeader("Content-type",
            "application/json;charset=utf-8");
        log.error(e.getMessage(), e);
        response.getWriter().write(
            formatErr(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage()));
    }

    private String formatErr(HttpStatus errType, String message) {
        return String.format(errResStr,
            errType.getReasonPhrase(),
            JSONObject.quote(message));
    }
}
