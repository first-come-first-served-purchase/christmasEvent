package com.doosan.christmas;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ChrismasApplication {
    public static void main(String[] args) {
        SpringApplication.run(ChrismasApplication.class, args);
    }
}