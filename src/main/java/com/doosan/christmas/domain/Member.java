package com.doosan.christmas.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.Hibernate;
import org.springframework.security.crypto.password.PasswordEncoder;
import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Builder
@Getter
@Setter
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
@Entity // JPA 엔티티 매핑
public class Member extends Timestamped implements Serializable { // 생성/수정 시간 자동 관리 클래스 상속

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동 생성 ID
    private Long id; // 회원 고유 ID

    @Column(unique = true) // 중복 불가
    private String nickname; // 회원 닉네임

    @Column(unique = true) // 중복 불가
    private String email; // 회원 이메일

    private String address; // 회원 주소

    @JsonIgnore // JSON 출력에서 제외
    @Column(nullable = false) // 필수 값
    private String password; // 회원 비밀번호

    @JsonIgnore // JSON 출력에서 제외
    @Builder.Default // 기본값 설정
    private boolean isDeleted = Boolean.FALSE; // 삭제 여부

    @Override
    public boolean equals(Object object) {
        // 동일성 검사 (ID 기준으로 비교)
        if (this == object) {
            return true;
        }
        if (object == null || Hibernate.getClass(this) != Hibernate.getClass(object)) {
            return false;
        }
        Member member = (Member) object;
        return id != null && Objects.equals(id, member.id);
    }

    @Override
    public int hashCode() {
        // 해시 코드 생성 (클래스와 ID 기반)
        return getClass().hashCode();
    }

    public boolean validatePassword(PasswordEncoder passwordEncoder, String password) {
        // 입력 비밀번호와 저장된 비밀번호 검증
        return passwordEncoder.matches(password, this.password);
    }
}
