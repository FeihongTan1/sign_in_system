package team.a9043.sign_in_system.other;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import team.a9043.sign_in_system.pojo.SisCourse;
import team.a9043.sign_in_system.pojo.SisJoinCourse;
import team.a9043.sign_in_system.pojo.SisUser;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author a9043
 */
@Slf4j
public class ForkJoinTest {
    @Test
    public void test() {
        LocalDateTime localDateTime = LocalDateTime.now();
        List<SisJoinCourse> sisUserList = IntStream.range(0, 2000)
            .parallel()
            .mapToObj(i -> {
                SisUser sisUser = new SisUser();
                sisUser.setSuId(String.valueOf(i));
                sisUser.setSuPassword("A");
                SisCourse sisCourse = new SisCourse();
                SisJoinCourse joinCourse = new SisJoinCourse();
                joinCourse.setSisUser(sisUser);
                joinCourse.setSisCourse(sisCourse);
                log.info(String.valueOf(i));
                return joinCourse;
            })
            .collect(Collectors.toList());
        log.info("until: " +
            localDateTime.until(LocalDateTime.now(), ChronoUnit.MICROS));

        localDateTime = LocalDateTime.now();
        sisUserList.parallelStream()
            .forEach(u -> u.getSisUser().setSuPassword(null));
        log.info("until: " +
            localDateTime.until(LocalDateTime.now(), ChronoUnit.MICROS));

        localDateTime = LocalDateTime.now();
        sisUserList
            .forEach(u -> u.getSisUser().setSuPassword(null));
        log.info("until: " +
            localDateTime.until(LocalDateTime.now(), ChronoUnit.MICROS));
    }
}
