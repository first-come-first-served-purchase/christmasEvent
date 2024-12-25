package com.doosan.christmas.service;

import com.doosan.christmas.domain.Member;
import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.*;
import com.doosan.christmas.dto.responsedto.MemberResponseDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.jwt.TokenProvider;
import com.doosan.christmas.repository.MemberRepository;
import com.doosan.christmas.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    // 닉네임 인증
    @Transactional
    public Object isPresentNickname(String nickname) {
        Optional<Member> optionalMember = memberRepository.findByNickname(nickname);
        return optionalMember.orElse(null);
    }

    // 회원가입
    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) throws IOException {

        //이메일 중복 체크
        if (null != isPresentMember(requestDto.getEmail())) {
            return ResponseDto.fail("DUPLICATED_EMAIL",
                    "중복된 이메일 입니다.");
        }

        // 이메일 형식 체크
        if(!requestDto.getEmail().contains("@")) {
            return ResponseDto.fail("INVALID_EMAIL",
                    "이메일 형식이 잘못 되었습니다.");
        }

        // 닉네임 중복 체크
        if(null != isPresentNickname(requestDto.getNickname())) {
            return ResponseDto.fail("INVALID_NICKNAME",
                    "중복된 닉네임 입니다.");
        }

        Member member = Member.builder()
                .nickname(requestDto.getNickname())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .email(requestDto.getEmail())
                .address(requestDto.getAddress())
                .build();
        memberRepository.save(member);

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .email(member.getEmail())
                        .address(member.getAddress())
                        .build()
        );
    }

    // 로그인
    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        Member member = isPresentMember(requestDto.getEmail());

        // null값 사용자 유효성 체크
        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        // 비밀번호 사용자 유효성 체크
        if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
            return ResponseDto.fail("INVALID_MEMBER", "이메일 혹은 비밀번호가 일치하지 않습니다.");
        }

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);

        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .modifiedAt(member.getModifiedAt())
                        .address(member.getAddress())
                        .email(member.getEmail())
                        .build()
        );
    }

    // 회원 이메일 유효성 인증
    @Transactional
    public Member isPresentMember(String email) {
        Optional<Member> optionalMember = memberRepository.findByEmail(email);
        return optionalMember.orElse(null);
    }

    // 로그아웃
    @Transactional
    public ResponseDto<?> logout(HttpServletRequest request) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh_Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }
        Member member = (Member) tokenProvider.getMemberFromAuthentication();

        if (null == member) {
            return ResponseDto.fail("MEMBER_NOT_FOUND",
                    "이메일 혹은 비밀번호가 일치하지 않습니다.");
        }
        return tokenProvider.deleteRefreshToken(member);
    }

    // 헤더에 담기는 토큰
    private void tokenToHeaders(TokenDto tokenDto, HttpServletResponse response) {
        response.addHeader("Authorization", "Bearer " + tokenDto.getAccessToken());
        response.addHeader("Refresh_Token", tokenDto.getRefreshToken());
        response.addHeader("Access-Token-Expire-Time", tokenDto.getAccessTokenExpiresIn().toString());
    }

    // 리이슈
    @Transactional
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        if (!tokenProvider.validateToken(request.getHeader("Refresh_Token"))) {
            return ResponseDto.fail("INVALID_TOKEN", "Token이 유효하지 않습니다.");
        }

        Member member = refreshTokenRepository.findByValue(request.getHeader("Refresh_Token")).get().getMember();

        TokenDto tokenDto = tokenProvider.generateTokenDto(member);
        tokenToHeaders(tokenDto, response);
        return ResponseDto.success(
                MemberResponseDto.builder()
                        .id(member.getId())
                        .nickname(member.getNickname())
                        .createdAt(member.getCreatedAt())
                        .address(member.getAddress())
                        .modifiedAt(member.getModifiedAt())
                        .email(member.getEmail())
                        .build()
        );
    }

    @Transactional
    public ResponseDto<?> withdrawMember(Long memberId, UserDetailsImpl userDetails) {
        log.info("회원 탈퇴 요청 시작: memberId = {}", memberId);

        // 인증된 사용자와 요청한 ID가 일치하는지 확인
        if (!memberId.equals(userDetails.getMember().getId())) {
            log.warn("요청한 ID와 인증된 사용자 ID가 일치하지 않습니다. 요청 ID: {}, 인증 ID: {}", memberId, userDetails.getMember().getId());
            return ResponseDto.fail("UNAUTHORIZED", "본인 계정만 탈퇴할 수 있습니다.");
        }

        // 사용자 조회
        Member member = memberRepository.findById(memberId).orElseThrow(() -> {
            log.error("등록되지 않은 회원: memberId = {}", memberId);
            return new IllegalArgumentException("등록되지 않은 회원입니다.");
        });
        log.debug("회원 탈퇴 대상 확인 완료: {}", member);

        // Refresh Token 삭제
        refreshTokenRepository.deleteByMemberId(memberId);
        log.info("Refresh token 삭제 완료: memberId = {}", memberId);

        // 사용자 삭제
        memberRepository.deleteById(memberId);
        log.info("계정 삭제 완료: memberId = {}", memberId);

        // 성공 응답 반환
        return ResponseDto.success("회원 탈퇴가 완료되었습니다.");
    }
}