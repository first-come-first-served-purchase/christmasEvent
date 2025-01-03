package com.doosan.christmas.member.domain;

import jakarta.persistence.*;
import lombok.*;


@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="refresh_token")
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    private Member member;

    @Column(nullable = false, unique = true)
    private String value;

    public void setValue(String refreshTokenValue) {

    }

    public Long getMemberId() {
        return member.getId();
    }
}
