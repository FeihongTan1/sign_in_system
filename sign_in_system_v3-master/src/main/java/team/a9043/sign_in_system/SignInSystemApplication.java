package team.a9043.sign_in_system;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@EnableAsync
@EnableScheduling
@MapperScan("team.a9043.sign_in_system.mapper")
public class SignInSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SignInSystemApplication.class, args);
    }
}
