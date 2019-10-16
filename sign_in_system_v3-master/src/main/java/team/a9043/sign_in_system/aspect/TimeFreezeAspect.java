package team.a9043.sign_in_system.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.stream.IntStream;

@Component
@Aspect
public class TimeFreezeAspect {
    public static LocalDateTime freezeTime = LocalDateTime
        .of(2018, Month.MAY, 30, 14, 35, 0);

    /*@Around(value = "execution(* team.a9043.sign_in_system.service.*.*" +
        "(..,java.time.LocalDateTime,..)))")*/
    public Object freezeDateTime(ProceedingJoinPoint pjp) throws Throwable {
        Signature signature = pjp.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;
        String[] parameterNames = methodSignature.getParameterNames();
        Object[] args = pjp.getArgs();
        Class[] argTypes = new Class[args.length];
        IntStream
            .range(0, args.length)
            .forEach(i -> argTypes[i] = (args[i] != null) ?
                args[i].getClass() : Object.class);
        IntStream
            .range(0, args.length)
            .filter(i -> (argTypes[i].equals(LocalDateTime.class) && parameterNames[i].equals("currentDateTime")))
            .forEach(i -> args[i] = freezeTime);
        //冻结 第七周 星期一 下午56节课 计网
        return pjp.proceed(args);
    }
}
