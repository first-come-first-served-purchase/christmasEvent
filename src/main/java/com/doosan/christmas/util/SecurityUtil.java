package com.doosan.christmas.util;

import com.doosan.christmas.domain.Member;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SecurityUtil {

    @Value("${aes.secretKey}")
    private String secretKey;

    public String encrypt(String input) throws Exception {
        return AESUtil.encrypt(input, secretKey);
    }

    public String decrypt(String input) throws Exception {
        return AESUtil.decrypt(input, secretKey);
    }

    public Member encryptMember(Member member) throws Exception {
        // Member 객체 필드 암호화 처리 로직
        member.setEmail(AESUtil.encrypt(member.getEmail(), secretKey));
        member.setNickname(AESUtil.encrypt(member.getNickname(), secretKey));
        member.setAddress(AESUtil.encrypt(member.getAddress(), secretKey));
        return member;
    }

    public Member decryptMember(Member member) throws Exception {
        // Member 객체 필드 복호화 처리 로직
        member.setEmail(AESUtil.decrypt(member.getEmail(), secretKey));
        member.setNickname(AESUtil.decrypt(member.getNickname(), secretKey));
        member.setAddress(AESUtil.decrypt(member.getAddress(), secretKey));
        return member;
    }
}
