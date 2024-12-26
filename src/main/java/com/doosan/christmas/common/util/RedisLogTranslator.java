package com.doosan.christmas.common.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class RedisLogTranslator {

    private static final Logger logger = LoggerFactory.getLogger(RedisLogTranslator.class);

    public void log(String message) {
        String translatedMessage = translateLogMessage(message);
        logger.info(translatedMessage);
    }

    private String translateLogMessage(String message) {

        if (message.contains("Redis is starting")) {

            return "Redis가 시작되었습니다.";

        } else if (message.contains("Ready to accept connections")) {

            return "연결 요청을 받을 준비가 완료되었습니다.";

        } else if (message.contains("Background saving started")) {

            return "백그라운드 저장이 시작되었습니다.";

        } else if (message.contains("DB saved on disk")) {

            return "DB가 디스크에 저장되었습니다.";
        }

        // 추가 번역 메시지 처리
        return message;
    }
}
