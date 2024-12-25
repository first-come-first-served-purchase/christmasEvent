package com.doosan.christmas.controller;

import com.doosan.christmas.domain.UserDetailsImpl;
import com.doosan.christmas.dto.requestdto.LoginRequestDto;
import com.doosan.christmas.dto.requestdto.MemberRequestDto;
import com.doosan.christmas.dto.responsedto.ResponseDto;
import com.doosan.christmas.service.MemberService;
import com.doosan.christmas.shared.Authority;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/v1/users")
public class memberController {
    private final MemberService memberService;

    //회원가입
    @PostMapping(value = "/signup")
    public ResponseDto<?> signup(@RequestBody @Valid MemberRequestDto requestDto) throws Exception {
        log.info("회원가입 요청 데이터 ################: {}", requestDto);
        log.debug("회원가입 요청 - 닉네임: {}, 이메일: {},  주소: {}, 비밀번호: {}, 비밀번호 확인: {}",
                requestDto.getNickname(), requestDto.getEmail(),
                requestDto.getAddress(), requestDto.getPassword(), requestDto.getPasswordConfirm());

        // 서비스 계층으로 전달
        return memberService.createMember(requestDto);
    }

    // 관리자 등록
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자만 접근 가능
    @PostMapping("/signup/admin")
    public ResponseDto<?> createAdmin(@RequestBody MemberRequestDto requestDto) {
        List<String> roles = List.of(Authority.ROLE_ADMIN); // 관리자 권한 부여
        return memberService.createMemberWithRoles(requestDto, roles);
    }

    // 판매자 등록
    @PreAuthorize("hasRole('ROLE_ADMIN')") // 관리자만 접근 가능
    @PostMapping("/signup/seller")
    public ResponseDto<?> createSeller(@RequestBody MemberRequestDto requestDto) {
        List<String> roles = List.of(Authority.ROLE_SELLER); // 판매자 권한 부여
        return memberService.createMemberWithRoles(requestDto, roles);
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