package com.doosan.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "`user`")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id // 기본 키 설정
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 증가 설정
    private int userId; // 사용자 ID

    private String username; // 사용자 이름

    private String email; // 사용자 이메일

    private String password; // 사용자 비밀번호

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role; // 사용자 역할 (USER 또는 ADMIN)

    @Column(nullable = false)
    private boolean emailVerified = false; // 이메일 인증 여부 (기본값: false)

    @PrePersist
    protected void onCreate() {
        this.role = Role.USER; // 새 사용자 생성 시 기본 역할을 USER로 설정
    }

    // 사용자 역할
    public enum Role {
        USER, // 일반 사용자
        ADMIN // 관리자
    }
}
