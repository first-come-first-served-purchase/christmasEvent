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
@RequestMapping(value = "/v1/members")
public class memberController {
    private final MemberService memberService;

    //회원가입
    @PostMapping(value="/signup")
    public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) throws IOException {
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
    @DeleteMapping(value = "/withdrawal/{memberId}")
    public ResponseDto<?> withdrawal(@PathVariable Long memberId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("회원 탈퇴 요청: userId = {}, 인증 사용자: {}", memberId, userDetails.getMember().getNickname());
        ResponseDto<?> result = memberService.withdrawMember(memberId, userDetails);
        log.info("회원 탈퇴 처리 완료: {}", result);
        return result;
    }
}