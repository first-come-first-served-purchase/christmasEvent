package com.doosan.christmas.controller;

import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.LoginRequestDto;
import com.doosan.christmas.dto.requestdto.MemberRequestDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users")
public class memberController {
    private final MemberService memberService;

    //회원가입
    @PostMapping(value = "/signup")
    public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) {
        // 요청 데이터 전체 로깅
        log.info("회원가입 요청 데이터 ################: {}", requestDto);

        // 세부 필드별 로깅
        log.debug("회원가입 요청 - 닉네임: {}, 이메일: {},  주소: {}, 비밀번호: {}, 비밀번호 확인: {}",
                requestDto.getNickname(), requestDto.getEmail(),
                requestDto.getAddress(), requestDto.getPassword(), requestDto.getPasswordConfirm());

        // 비밀번호와 비밀번호 확인 체크

        if (!requestDto.getPassword().equals(requestDto.getPasswordConfirm())) {
            log.warn("비밀번호 불일치 - password: {}, passwordConfirm: {}", requestDto.getPassword(), requestDto.getPasswordConfirm());
        }

        // 서비스 계층으로 전달
        return memberService.createMember(requestDto);
    }


    //로그인
    @PostMapping(value = "/login")
    public ResponseDto<?> login(@RequestBody @Valid LoginRequestDto requestDto, HttpServletResponse response) {
        return memberService.login(requestDto, response);
    }

    //로그아웃
    @PostMapping(value = "/logout")
    public ResponseDto<?> logout(HttpServletRequest request) {
        return memberService.logout(request);
    }

    //토큰재발급
    @PostMapping(value = "/reissue")
    public ResponseDto<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        return memberService.reissue(request, response);
    }

    // 회원 탈퇴
    @DeleteMapping(value = "/delete/{memberId}")
    public ResponseDto<?> delete(@PathVariable Long memberId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("회원 탈퇴 요청: userId = {}, 인증 사용자: {}", memberId, userDetails.getMember().getNickname());
        ResponseDto<?> result = memberService.memberDelete(memberId, userDetails);
        log.info("회원 탈퇴 처리 완료: {}", result);
        return result;
    }
}