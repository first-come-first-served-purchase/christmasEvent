package com.doosan.christmas.user.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@EntityScan(basePackages = "com.doosan.christmas.user.domain")
@EnableJpaRepositories(basePackages = "com.doosan.christmas.user.repository")
public class JpaConfig {
}