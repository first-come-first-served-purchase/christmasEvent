package com.doosan.christmas.api.dto.member;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MemberRequestDto {
    private String email;
    private String password;
    private String nickname;
    private String address;
} 