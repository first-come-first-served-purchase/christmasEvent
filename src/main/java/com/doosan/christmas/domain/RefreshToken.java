package com.doosan.christmas.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
public class RefreshToken extends Timestamped {
    // 생성/수정 시간 자동 관리 클래스 상속
    @Id
    @Column(nullable = false) // 필수 값
    private Long id; // RefreshToken의 고유 ID

    @JoinColumn(name = "member_id", referencedColumnName = "id") // Member와 연관된 컬럼
    @OneToOne(fetch = FetchType.LAZY) // Member와 1:1 관계, 지연 로딩
    private Member member; // 연결된 사용자

    @Column(nullable = false) // 필수 값
    private String value; // 리프레시 토큰 값
}
