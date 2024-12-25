package com.doosan.christmas.service;

import com.doosan.christmas.domain.Member;
import com.doosan.christmas.domain.RefreshToken;
import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.*;
import com.doosan.christmas.dto.responsedto.MemberResponseDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.jwt.TokenProvider;
import com.doosan.christmas.repository.MemberRepository;
import com.doosan.christmas.repository.RefreshTokenRepository;
import com.doosan.christmas.shared.Authority;
import com.doosan.christmas.util.AESUtil;
import com.doosan.christmas.util.Base64Util;
import com.doosan.christmas.util.RedisUtil;
import com.doosan.christmas.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    private final SecurityUtil securityUtil;

    @Value("${aes.secretKey}")
    private String secretKey; // AES 암호화 키

    @Transactional
    public ResponseDto<?> createMember(MemberRequestDto requestDto) throws Exception {
        logger.info("서비스로 전달된 회원가입 데이터: {}", requestDto);

        // 이메일 인증 확인
        if (!authService.isEmailVerified(requestDto.getEmail())) {
            logger.warn("이메일 인증 실패 - 이메일: {}, 이유: Redis에 인증 상태가 없음 또는 이메일 불일치", requestDto.getEmail());
            return ResponseDto.fail("EMAIL_NOT_VERIFIED", "이메일 인증이 완료 되지 않았습니다.");
        }

        // 이메일 중복 확인
        if (memberRepository.findByEmail(securityUtil.encrypt(requestDto.getEmail())).isPresent()) {
            return ResponseDto.fail("EMAIL_DUPLICATED", "이미 사용 중인 이메일입니다.");
        }

        // 닉네임 중복 확인
        if (memberRepository.findByNickname(securityUtil.encrypt(requestDto.getNickname())).isPresent()) {
            return ResponseDto.fail("NICKNAME_DUPLICATED", "이미 사용 중인 닉네임입니다.");
        }

        // 비밀번호와 비밀번호 확인 체크
        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            log.warn("비밀번호 불일치 - password: {}, passwordConfirm: {}", requestDto.getPassword(), requestDto.getPasswordConfirm());
        }

        // 기본 권한 설정
        List<String> roles = new ArrayList<>();
        roles.add(Authority.ROLE_USER);

        // 특정 이메일인 경우 관리자 권한 추가
        if ("doosan0000425@gmail.com".equals(requestDto.getEmail())) {
            roles.clear(); // 이전 역할 제거
            roles.add(Authority.ROLE_ADMIN);
            log.info("관리자 권한 부여 - 이메일: {}", requestDto.getEmail());
        }

        log.info("Roles 설정 확인 - email: {}, roles: {}", requestDto.getEmail(), roles);

        try {
            // 암호화 전 원본 데이터 로깅
            logger.debug("암호화 전 원본 데이터 - 이메일: {}, 닉네임: {}, 주소: {}",
                    requestDto.getEmail(), requestDto.getNickname(), requestDto.getAddress());

            // 암호화 해서 저장
            String encryptedEmail = securityUtil.encrypt(requestDto.getEmail());
            logger.debug("암호화된 이메일: {}", encryptedEmail); // 로그 추가

            String encryptedNickname = securityUtil.encrypt(requestDto.getNickname());
            logger.debug("암호화된 닉네임: {}", encryptedNickname); // 로그 추가

            String encryptedAddress = securityUtil.encrypt(requestDto.getAddress());
            logger.debug("암호화된 주소: {}", encryptedAddress); // 로그 추가

            // 암호화 후 데이터 로깅
            logger.debug("암호화 후 데이터 - 암호화 이메일: {}, 암호화 닉네임: {}, 암호화 주소: {}",
                    encryptedEmail, encryptedNickname, encryptedAddress);

            // 회원 생성 및 저장
            Member member = Member.builder()
                    .email(encryptedEmail) // 암호화된 이메일 저장
                    .nickname(encryptedNickname) // 암호화된 닉네임 저장
                    .password(passwordEncoder.encode(requestDto.getPassword()))
                    .address(encryptedAddress) // 암호화된 주소 저장
                    .roles(roles) // 권한 설정
                    .build();

            memberRepository.save(member);

            // 회원 저장 완료 로그
            logger.info("회원가입 완료 - 암호화 이메일: {}, 암호화 닉네임: {}, 권한: {}",
                    member.getEmail(), member.getNickname(), member.getRoles());

            return ResponseDto.success("회원 가입이 완료 되었습니다.");

        } catch (Exception e) {
            // 예외 발생 시 상세 로그 출력
            logger.error("암호화 처리 중 오류 발생 - 요청 데이터: {}, 예외 메시지: {}, 예외 스택: {}",
                    requestDto, e.getMessage(), e);
            return ResponseDto.fail("ENCRYPTION_FAILED", "회원가입 중 암호화에 실패했습니다.");
        }

    }


    // 로그인
    @Transactional
    public ResponseDto<?> login(LoginRequestDto requestDto, HttpServletResponse response) {
        logger.info("로그인 요청 시작 - 이메일: {}", requestDto.getEmail());

        try {
            // 입력 이메일 암호화
            String encryptedEmail = securityUtil.encrypt(requestDto.getEmail());
            logger.debug("암호화된 이메일: {}", encryptedEmail);

            // 사용자 검색
            Optional<Member> optionalMember = memberRepository.findByEmail(encryptedEmail);
            if (optionalMember.isEmpty()) {
                logger.warn("사용자를 찾을 수 없습니다 - 이메일: {}", requestDto.getEmail());
                return ResponseDto.fail("LOGIN_FAILED", "이메일 혹은 비밀번호가 일치하지 않습니다.");
            }
            Member member = optionalMember.get();

            // 비밀번호 검증
            if (!member.validatePassword(passwordEncoder, requestDto.getPassword())) {
                logger.warn("비밀번호 불일치 - 이메일: {}", requestDto.getEmail());
                return ResponseDto.fail("LOGIN_FAILED", "이메일 혹은 비밀번호가 일치하지 않습니다.");
            }

            // 토큰 생성 및 반환
            TokenDto tokenDto = tokenProvider.generateTokenDto(member);
            tokenToHeaders(tokenDto, response); // HttpServletResponse로 헤더 설정
            return ResponseDto.success("로그인 성공");

        } catch (Exception e) {
            logger.error("로그인 중 예외 발생 - 이메일: {}, 에러: {}", requestDto.getEmail(), e.getMessage());
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

    // 리이슈
    @Transactional
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        logger.info("토큰 재발급 요청 시작");

        try {
            // 헤더에서 RefreshToken 추출
            String refreshTokenValue = request.getHeader("Authorization");

            if (refreshTokenValue == null || refreshTokenValue.isEmpty()) {
                logger.warn("요청에 RefreshToken이 없음");
                return ResponseDto.fail("TOKEN_MISSING", "요청에 토큰이 포함되지 않았습니다.");
            }

            logger.debug("요청에서 추출한 RefreshToken: {}", refreshTokenValue);

            // 'Bearer ' 접두사 제거
            if (refreshTokenValue.startsWith("Bearer ")) {
                refreshTokenValue = refreshTokenValue.substring(7);
            }

            // RefreshToken 조회
            Optional<RefreshToken> optionalRefreshToken = refreshTokenRepository.findByValue(refreshTokenValue);

            if (optionalRefreshToken.isEmpty()) {
                logger.warn("유효하지 않은 RefreshToken: {}", refreshTokenValue);
                return ResponseDto.fail("TOKEN_INVALID", "유효하지 않은 토큰입니다.");
            }

            RefreshToken refreshToken = optionalRefreshToken.get();

            // Member 조회
            Optional<Member> optionalMember = memberRepository.findById(refreshToken.getMemberId());
            if (optionalMember.isEmpty()) {
                logger.warn("Member를 찾을 수 없음 - RefreshToken: {}", refreshTokenValue);
                return ResponseDto.fail("MEMBER_NOT_FOUND", "사용자를 찾을 수 없습니다.");
            }

            Member member = optionalMember.get();

            // 새 토큰 생성
            TokenDto tokenDto = tokenProvider.generateTokenDto(member);

            // 새 토큰을 HttpServletResponse 헤더에 추가
            tokenToHeaders(tokenDto, response);

            logger.info("새 토큰 발급 성공 - Member ID: {}", member.getId());

            return ResponseDto.success(tokenDto);
        } catch (Exception e) {
            logger.error("토큰 재발급 중 오류 발생: {}", e.getMessage(), e);
            return ResponseDto.fail("TOKEN_REISSUE_FAILED", "토큰 재발급에 실패했습니다.");
        }
    }


}