package team.a9043.sign_in_system.security.tokenuser;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import team.a9043.sign_in_system.pojo.SisUser;
import team.a9043.sign_in_system.security.entity.SisAuthenticationToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.IntStream;

@Component
@Aspect
@Order()
public class TokenUserAspect {
    @Around(value = "execution(" +
        "* team.a9043.sign_in_system.controller.*.*(..,@TokenUser (*), ..))",
        argNames = "pjp")
    public Object getUser(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Method method = signature.getMethod();
        Annotation[][] methodAnnotations = method.getParameterAnnotations();
        Object[] args = pjp.getArgs();
        Class[] argTypes = Arrays.stream(args).map(arg -> arg != null ?
            arg.getClass() : null).toArray(Class[]::new);
        int userArgsIdx;

        userArgsIdx = IntStream.range(0, args.length)
            .filter(i -> argTypes[i].equals(SisUser.class))
            .filter(i -> Arrays.stream(methodAnnotations[i]).anyMatch(annotation -> annotation.annotationType().equals(TokenUser.class)))
            .findFirst()
            .orElse(-1);

        args[userArgsIdx] =
            ((SisAuthenticationToken) SecurityContextHolder.getContext().getAuthentication()).getSisUser();
        return pjp.proceed(args);
    }
}
