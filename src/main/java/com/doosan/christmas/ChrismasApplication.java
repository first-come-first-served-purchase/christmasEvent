package com.doosan.christmas;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling  // @Scheduled를 활성화
public class ChrismasApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChrismasApplication.class, args);
    }
}