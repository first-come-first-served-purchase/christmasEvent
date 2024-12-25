package com.doosan.christmas.dto.responsedto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberResponseDto  {
    private Long id; // 회원 ID
    private String nickname; // 닉네임
    private String email; // 이메일
    private String address; // 주소
    private LocalDateTime createdAt; // 생성 시간
    private LocalDateTime modifiedAt; // 수정 시간
    private List<String> roles; // 사용자 권한
}