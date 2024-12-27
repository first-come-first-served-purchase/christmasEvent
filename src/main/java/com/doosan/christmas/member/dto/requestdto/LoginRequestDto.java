package com.doosan.christmas.member.dto.requestdto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    @NotBlank(message = "이메일을 입력해주세요.")
    private String email; // 이메일

    @NotBlank(message = "비밀번호를 입력해주세요.")
    private String password; // 패스워드
}
