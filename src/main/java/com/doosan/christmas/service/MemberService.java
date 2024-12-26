package com.doosan.christmas.service;

import com.doosan.christmas.domain.Member;
import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.*;
import com.doosan.christmas.dto.responsedto.MemberResponseDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.jwt.TokenProvider;
import com.doosan.christmas.repository.MemberRepository;
import com.doosan.christmas.repository.RefreshTokenRepository;
import com.doosan.christmas.shared.Authority;
import com.doosan.christmas.util.RedisUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Base64;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {
    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final MailSendService mailSendService;
    private final AuthService authService;
    private final RedisUtil redisUtil;

    // 암호화 메서드
    private String encrypt(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        return Base64.getEncoder().encodeToString(data.getBytes());
    }

    // 복호화 메서드
    private String decrypt(String encryptedData) {
        if (encryptedData == null || encryptedData.isEmpty()) {
            return encryptedData;
        }
        return new String(Base64.getDecoder().decode(encryptedData));
    }

    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) {
        try {
            // 이메일 인증 확인
            if (!authService.isEmailVerified(requestDto.getEmail())) {
                return ResponseDto.fail("EMAIL_NOT_VERIFIED", "이메일 인증이 완료되지 않았습니다.");
            }

            // 암호화하여 저장
            String encryptedEmail = encrypt(requestDto.getEmail());
            String encryptedNickname = encrypt(requestDto.getNickname());
            String encryptedAddress = encrypt(requestDto.getAddress());

            Member member = Member.builder()
                    .email(encryptedEmail)
                    .nickname(encryptedNickname)
                    .password(passwordEncoder.encode(requestDto.getPassword()))
                    .address(encryptedAddress)
                    .roles(determineRoles(requestDto.getEmail()))
                    .build();

            memberRepository.save(member);

            // 응답할 때는 복호화해서 반환
            return ResponseDto.success(
                    MemberResponseDto.builder()
                            .id(member.getId())
                            .email(decrypt(member.getEmail()))
                            .nickname(decrypt(member.getNickname()))
                            .address(decrypt(member.getAddress()))
                            .roles(member.getRoles())
                            .createdAt(member.getCreatedAt())
                            .modifiedAt(member.getModifiedAt())
                            .build()
            );
        } catch (Exception e) {
            log.error("회원가입 실패", e);
            return ResponseDto.fail("SIGNUP_FAILED", "회원가입에 실패했습니다.");
        }
    }

    private List<String> determineRoles(String email) {
        List<String> roles = new ArrayList<>();
        if ("doosan0000425@gmail.com".equals(email)) {
            roles.add(Authority.ROLE_ADMIN);
        } else {
            roles.add(Authority.ROLE_USER);
        }
        return roles;
    }

    // 로그인
    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        try {
            String encryptedEmail = encrypt(requestDto.getEmail());
            Member member = memberRepository.findByEmail(encryptedEmail)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

            if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
                return ResponseDto.fail("INVALID_MEMBER", "이메일 혹은 비밀번호가 일치하지 않습니다.");
            }

            TokenDto tokenDto = tokenProvider.generateTokenDto(member);
            tokenToHeaders(tokenDto, response);

            return ResponseDto.success(
                    MemberResponseDto.builder()
                            .id(member.getId())
                            .email(decrypt(member.getEmail()))
                            .nickname(decrypt(member.getNickname()))
                            .address(decrypt(member.getAddress()))
                            .roles(member.getRoles())
                            .createdAt(member.getCreatedAt())
                            .modifiedAt(member.getModifiedAt())
                            .build()
            );
        } catch (Exception e) {
            log.error("로그인 실패", e);
            return ResponseDto.fail("LOGIN_FAILED", "로그인에 실패했습니다.");
        }
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
                        .nickname(member.getNickname()) // 닉네임
                        .email(member.getEmail()) // 이메일
                        .address(member.getAddress()) // 주소
                        .roles(member.getRoles()) // 권한
                        .createdAt(member.getCreatedAt()) // 생성시간
                        .modifiedAt(member.getModifiedAt()) // 수정시간
                        .build()
        );
    }

    // 회원 탈퇴
    @Transactional
    public ResponseDto<?> memberDelete(Long memberId, UserDetailsImpl userDetails) {
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


    // 이메일 인증 요청
    @Transactional
    public ResponseDto<?> sendEmailAuth(EmailAuthRequestDTO requestDTO) {
        try {
            String authCode = authService.sendAuthEmail(requestDTO.getEmail());
            logger.info("인증 코드 전송 완료 - 이메일: {}, 인증 코드: {}", requestDTO.getEmail(), authCode);
            return ResponseDto.success("이메일 인증 코드가 발송되었습니다.");
        } catch (Exception e) {
            logger.error("이메일 인증 코드 발송 중 오류 발생 - 이메일: {}, 오류 메시지: {}",
                    requestDTO.getEmail(), e.getMessage(), e);
            return ResponseDto.fail("SERVER_ERROR", "이메일 인증 코드 발송 중 오류가 발생했습니다.");
        }
    }




    public ResponseDto<?> verifyEmailCode(EmailAuthRequestDTO requestDTO) {
        if (authService.checkAuthNum(requestDTO.getEmail(), requestDTO.getAuthCode())) {
            return ResponseDto.success("이메일 인증이 완료되었습니다.");
        }
        return ResponseDto.fail("VERIFICATION_FAILED", "인증번호가 일치하지 않습니다.");
    }

    // 관리자 등록
    @Transactional
    public ResponseDto<?> createMemberWithRoles(MemberRequestDto requestDto, List<String> roles) {
        log.info("회원가입 요청 데이터: {}, roles: {}", requestDto, roles);

        // 이메일 중복 확인
        if (memberRepository.findByEmail(requestDto.getEmail()).isPresent()) {
            return ResponseDto.fail("EMAIL_DUPLICATED", "이미 사용 중인 이메일입니다.");
        }

        // 회원 생성
        Member member = Member.builder()
                .nickname(requestDto.getNickname())
                .email(requestDto.getEmail())
                .password(passwordEncoder.encode(requestDto.getPassword()))
                .address(requestDto.getAddress())
                .roles(roles) // 권한 설정
                .build();

        memberRepository.save(member);
        log.info("회원가입 완료 - email: {}, roles: {}", member.getEmail(), member.getRoles());

        return ResponseDto.success("회원 가입이 완료되었습니다.");
    }

}