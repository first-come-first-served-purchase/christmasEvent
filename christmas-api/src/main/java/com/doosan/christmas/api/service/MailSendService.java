package com.doosan.christmas.api.service;

import com.doosan.christmas.common.util.RedisUtil;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class MailSendService {

    private static final Logger logger = LoggerFactory.getLogger(MailSendService.class);

    private final RedisUtil redisUtil;
    private final JavaMailSender mailSender;
    private String authNum;

    @Autowired
    public MailSendService(RedisUtil redisUtil, JavaMailSender mailSender) {
        this.redisUtil = redisUtil;
        this.mailSender = mailSender;
    }

    // 기존 메서드들...
    // createCode(), createEmailForm(), sendAuthEmail() 메서드들
} 