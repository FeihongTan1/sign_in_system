package team.a9043.sign_in_system.aspect;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author a9043
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Slf4j
public class SupervisionAspectTest {
    @Resource
    private SupervisionAspect supervisionAspect;

    @Test
    public void updateAttRate() {
        supervisionAspect.updateAttRate(138);
    }

    @Test
    public void updateAttRateAll() {
        supervisionAspect.updateAttRate();
    }
}
