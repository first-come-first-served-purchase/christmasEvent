package com.doosan.christmas.api.dto.member;

import com.doosan.christmas.common.domain.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String address;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .address(member.getAddress())
                .build();
    }
} 