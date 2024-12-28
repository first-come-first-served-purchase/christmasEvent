package com.doosan.christmas.api.service;

import com.doosan.christmas.api.dto.member.LoginResponseDto;
import com.doosan.christmas.api.dto.member.MemberResponseDto;
import com.doosan.christmas.api.dto.member.SignupRequestDto;
import com.doosan.christmas.common.domain.Member;
import com.doosan.christmas.common.dto.TokenDto;
import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.common.repository.MemberRepository;
import com.doosan.christmas.common.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    @Transactional
    public void signup(SignupRequestDto request) {
        // 이메일 중복 검사
        if (memberRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new CustomException(ErrorCode.DUPLICATE_EMAIL);
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .address(request.getAddress())
                .build();

        memberRepository.save(member);
    }

    @Transactional
    public LoginResponseDto login(String email, String password) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        if (!passwordEncoder.matches(password, member.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        MemberResponseDto memberResponseDto = MemberResponseDto.from(member);

        return LoginResponseDto.from(tokenDto, memberResponseDto);
    }

    @Transactional
    public void logout(String refreshToken) {
        // 리프레시 토큰 검증
        if (!tokenProvider.validateToken(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        
        // 토큰에서 회원 정보 추출
        Member member = tokenProvider.getMemberFromAuthentication();
        tokenProvider.deleteRefreshToken(member);
    }
} 