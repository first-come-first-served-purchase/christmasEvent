package com.doosan.christmas.user;
import com.doosan.christmas.user.config.JwtConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableDiscoveryClient
@SpringBootApplication
@EnableJpaAuditing
@ComponentScan(basePackages = {
    "com.doosan.christmas.user",
    "com.doosan.christmas.common"
})
@EnableConfigurationProperties(JwtConfig.class)
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
} 