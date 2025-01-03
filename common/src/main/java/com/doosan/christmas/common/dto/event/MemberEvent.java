package com.doosan.christmas.common.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEvent {
    private Long memberId;
    private String email;
    private String eventType;  // "CREATED", "UPDATED", "DELETED" 등
    private Long timestamp;
    
    // 다른 서비스들이 필요로 하는 최소한의 회원 정보만 포함
} 