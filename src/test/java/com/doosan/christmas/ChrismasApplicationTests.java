package com.doosan.christmas;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = {
    "spring.redis.host=localhost",
    "spring.redis.port=6379",
    "spring.mail.host=smtp.naver.com",
    "spring.mail.port=465",
    "spring.mail.username=${MAIL_USERNAME}",
    "spring.mail.password=${MAIL_PASSWORD}"
})
class ChrismasApplicationTests {

    @Test
    void contextLoads() {
        // 기본 컨텍스트 로드 테스트
    }

}
