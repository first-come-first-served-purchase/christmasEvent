package com.doosan.userservice.service;

import com.doosan.common.dto.ResponseMessage;
import com.doosan.common.exception.BusinessRuntimeException;
import com.doosan.userservice.JwtUtil;
import com.doosan.userservice.dto.EmailAuthRequestDto;
import com.doosan.userservice.dto.LoginRequestDto;
import com.doosan.userservice.dto.PasswordChangeRequestDto;
import com.doosan.userservice.dto.SignupRequestDto;
import com.doosan.userservice.dto.TokenDto;
import com.doosan.userservice.entity.User;
import com.doosan.userservice.repository.UserRepository;
import com.doosan.userservice.util.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtil redisUtil;
    private final MailSendService mailSendService;

    @Override
    public ResponseEntity<ResponseMessage> sendEmailAuth(EmailAuthRequestDto requestDto) {
        try {
            // 이메일 인증 코드 전송
            String authCode = mailSendService.sendAuthEmail(requestDto.getEmail());
            log.info("이메일 인증 코드 전송 완료: 이메일={}, 인증 코드={}", requestDto.getEmail(), authCode);

            // Redis에 저장
            redisUtil.setDataExpire(authCode, requestDto.getEmail(), 600);

            // 성공 응답 메시지 생성
            ResponseMessage responseMessage = ResponseMessage.builder()
                    .data(null) // 필요에 따라 데이터 추가
                    .statusCode(200) // HTTP OK 상태
                    .resultMessage("SUCCESS")
                    .detailMessage("인증 코드가 이메일로 전송되었습니다.")
                    .build();

            return ResponseEntity.ok(responseMessage);
        } catch (Exception e) {
            log.error("이메일 인증 코드 전송 실패: {}", e.getMessage());

            // 실패 응답 메시지 생성
            ResponseMessage errorResponse = ResponseMessage.builder()
                    .data(null) // 필요에 따라 데이터 추가
                    .statusCode(400) // HTTP Bad Request 상태
                    .resultMessage("FAIL")
                    .detailMessage("이메일 인증 코드 전송 중 오류가 발생했습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @Override
    public ResponseEntity<ResponseMessage> verifyEmailCode(EmailAuthRequestDto requestDTO) {
        String savedEmail = redisUtil.getData(requestDTO.getAuthCode());
        if (savedEmail == null || !savedEmail.equals(requestDTO.getEmail())) {
            log.error("인증 코드 검증 실패: 입력된 이메일과 저장된 데이터가 일치하지 않습니다.");
            
            ResponseMessage errorResponse = ResponseMessage.builder()
                    .statusCode(400)
                    .resultMessage("FAIL")
                    .detailMessage("인증 코드 검증 실패: 입력된 이메일과 저장된 데이터가 일치하지 않습니다.")
                    .build();

            return ResponseEntity.badRequest().body(errorResponse);
        }

        // 인증 코드가 유효한 경우
        redisUtil.deleteData(requestDTO.getAuthCode());
        // 이메일 인증 완료 표시를 Redis에 저장 (30분 유효)
        redisUtil.setDataExpire(requestDTO.getEmail() + ":verified", "true", 1800);
        
        log.info("인증 코드 검증 성공: 이메일={}", requestDTO.getEmail());

        ResponseMessage successResponse = ResponseMessage.builder()
                .statusCode(200)
                .resultMessage("SUCCESS")
                .detailMessage("인증 코드 검증 성공")
                .build();

        return ResponseEntity.ok(successResponse);
    }


    @Override
    public ResponseEntity<ResponseMessage> signup(SignupRequestDto createUserRequestDto) {
        try {
            // 이메일 중복 체크
            if (userRepository.findByEmail(createUserRequestDto.getEmail()).isPresent()) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("회원가입 실패")
                        .detailMessage("이미 사용중인 이메일입니다.")
                        .build()
                );
            }

            // 이메일 인증 여부 체크
            String authCode = redisUtil.getData(createUserRequestDto.getEmail() + ":verified");
            if (authCode == null) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("회원가입 실패")
                        .detailMessage("이메일 인증이 필요합니다.")
                        .build()
                );
            }

            // 비밀번호 유효성 검사
            String password = createUserRequestDto.getPassword();
            if (!isPasswordValid(password)) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("회원가입 실패")
                        .detailMessage("비밀번호는 8~16자의 영문, 숫자, 특수문자를 포함해야 합니다.")
                        .build()
                );
            }

            // 사용자 생성
            User user = new User();
            user.setEmail(createUserRequestDto.getEmail());
            user.setPassword(passwordEncoder.encode(createUserRequestDto.getPassword()));
            user.setUsername(createUserRequestDto.getUsername());
            user.setEmailVerified(true);

            // Redis에서 인증 정보 삭제
            redisUtil.deleteData(createUserRequestDto.getEmail() + ":verified");

            User savedUser = saveUser(user);

            return ResponseEntity.status(201).body(
                ResponseMessage.builder()
                    .data(savedUser)
                    .statusCode(201)
                    .resultMessage("회원가입 성공")
                    .build()
            );

        } catch (DataAccessException e) {
            log.error("회원가입 처리 중 데이터베이스 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("데이터베이스 처리 중 오류가 발생했습니다.")
                    .build()
            );
        } catch (Exception e) {
            log.error("회원가입 처리 중 예기치 않은 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("회원가입 처리 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    @Override
    public ResponseEntity<ResponseMessage> login(LoginRequestDto loginRequestDto) {
        try {
            Optional<User> user = userRepository.findByEmail(loginRequestDto.getEmail());
            if (user.isEmpty()) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("로그인 실패")
                        .detailMessage("존재하지 않는 유저입니다.")
                        .build()
                );
            }

            if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.get().getPassword())) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("로그인 실패")
                        .detailMessage("잘못된 비밀번호입니다.")
                        .build()
                );
            }

            // 토큰 생성
            TokenDto tokenDto = jwtUtil.createTokenDto(user.get().getUserId(), user.get().getRole().toString());
            
            // Redis에 RefreshToken 저장 (7일)
            redisUtil.setDataExpire(
                "RT:" + user.get().getUserId(),
                tokenDto.getRefreshToken(),
                7 * 24 * 60 * 60L
            );

            return ResponseEntity.ok(
                ResponseMessage.builder()
                    .data(tokenDto)
                    .statusCode(200)
                    .resultMessage("로그인 성공")
                    .build()
            );

        } catch (Exception e) {
            log.error("로그인 처리 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("로그인 처리 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    @Override
    public ResponseEntity<ResponseMessage> logout(String token) {
        try {
            if (token != null && token.startsWith("Bearer ")) {
                String jwt = token.substring(7);
                // 토큰의 남은 유효시간 계산
                long remainingTime = jwtUtil.getRemainingTime(jwt);
                
                if (remainingTime > 0) {
                    // Redis에 토큰을 블랙리스트로 등록
                    redisUtil.setDataExpire(jwt, "logout", remainingTime);
                }
            }

            return ResponseEntity.ok(ResponseMessage.builder()
                    .statusCode(200)
                    .resultMessage("로그아웃 성공")
                    .build());

        } catch (Exception e) {
            log.error("로그아웃 처리 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("로그아웃 처리 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    @Override
    public ResponseEntity<ResponseMessage> changePassword(String token, PasswordChangeRequestDto passwordChangeRequestDto) {
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ResponseEntity.status(401).body(
                    ResponseMessage.builder()
                        .statusCode(401)
                        .resultMessage("인증 실패")
                        .detailMessage("유효하지 않은 토큰입니다.")
                        .build()
                );
            }

            String jwt = token.substring(7);
            int userId = jwtUtil.getUserIdFromToken(jwt);
            
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuntimeException("사용자를 찾을 수 없습니다."));

            // 현재 비밀번호 확인
            if (!passwordEncoder.matches(passwordChangeRequestDto.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("비밀번호 변경 실패")
                        .detailMessage("현재 비밀번호가 일치하지 않습니다.")
                        .build()
                );
            }

            // 새 비밀번호 유효성 검사
            if (!isPasswordValid(passwordChangeRequestDto.getNewPassword())) {
                return ResponseEntity.status(400).body(
                    ResponseMessage.builder()
                        .statusCode(400)
                        .resultMessage("비밀번호 변경 실패")
                        .detailMessage("새 비밀번호가 정책에 부합하지 않습니다.")
                        .build()
                );
            }

            // 비밀번호 업데이트
            user.setPassword(passwordEncoder.encode(passwordChangeRequestDto.getNewPassword()));
            userRepository.save(user);

            // 모든 기존 토큰을 무효화하기 위해 사용자의 토큰 버전 업데이트
            String userTokenKey = "user:" + userId + ":tokens";
            redisUtil.deleteData(userTokenKey);

            return ResponseEntity.ok(ResponseMessage.builder()
                    .statusCode(200)
                    .resultMessage("비밀번호 변경 성공")
                    .detailMessage("모든 기기에서 로그아웃되었습니다.")
                    .build());

        } catch (Exception e) {
            log.error("비밀번호 변경 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("비밀번호 변경 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    @Override
    public ResponseEntity<ResponseMessage> reissueToken(String accessToken, String refreshToken) {
        try {
            // Access Token에서 User ID 추출
            if (!accessToken.startsWith(JwtUtil.BEARER_PREFIX)) {
                throw new BusinessRuntimeException("유효하지 않은 토큰 형식입니다.");
            }
            
            String token = accessToken.substring(7);
            int userId = jwtUtil.getUserIdFromToken(token);

            // Redis에서 저장된 Refresh Token 값 가져오기
            String savedRefreshToken = redisUtil.getData("RT:" + userId);
            if (savedRefreshToken == null || !savedRefreshToken.equals(refreshToken)) {
                throw new BusinessRuntimeException("유효하지 않은 Refresh Token입니다.");
            }

            // User 정보 가져오기
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessRuntimeException("사용자를 찾을 수 없습니다."));

            // 새로운 토큰 발급
            TokenDto newTokenDto = jwtUtil.createTokenDto(userId, user.getRole().toString());

            // Redis에 새로운 Refresh Token 저장
            redisUtil.setDataExpire(
                "RT:" + userId,
                newTokenDto.getRefreshToken(),
                7 * 24 * 60 * 60L
            );

            return ResponseEntity.ok(ResponseMessage.builder()
                    .data(newTokenDto)
                    .statusCode(200)
                    .resultMessage("토큰 재발급 성공")
                    .build());

        } catch (BusinessRuntimeException e) {
            return ResponseEntity.status(400).body(
                ResponseMessage.builder()
                    .statusCode(400)
                    .resultMessage("토큰 재발급 실패")
                    .detailMessage(e.getMessage())
                    .build()
            );
        } catch (Exception e) {
            log.error("토큰 재발급 중 오류 발생", e);
            return ResponseEntity.status(500).body(
                ResponseMessage.builder()
                    .statusCode(500)
                    .resultMessage("서버 오류")
                    .detailMessage("토큰 재발급 중 오류가 발생했습니다.")
                    .build()
            );
        }
    }

    private boolean isPasswordValid(String password) {
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*?_])[A-Za-z\\d!@#$%^&*?_]{8,16}$";
        return password.matches(passwordPattern);
    }

    private User saveUser(User user) {
        try {
            return userRepository.save(user);
        } catch (DataAccessException e) {
            log.error("회원 저장 중 데이터베이스 오류 발생", e);
            throw new BusinessRuntimeException("회원 저장에 실패했습니다.", e);
        } catch (Exception e) {
            log.error("회원 저장 중 예기치 않은 오류 발생", e);
            throw new BusinessRuntimeException("회원 저장 중 예기치 않은 오류가 발생했습니다.", e);
        }
    }
}
