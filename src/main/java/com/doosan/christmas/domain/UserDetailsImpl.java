package com.doosan.christmas.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.ArrayList;
import java.util.Collection;

@Data
@NoArgsConstructor // 기본 생성자
@AllArgsConstructor // 모든 필드를 포함한 생성자
public class UserDetailsImpl implements UserDetails { // Spring Security의 UserDetails 구현
    private Member member; // 연관된 회원 정보

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 사용자 권한 반환 (현재 비어 있음)
        Collection<GrantedAuthority> authorities = new ArrayList<>();
        return authorities;
    }

    @Override
    public String getPassword() {
        // 회원 비밀번호 반환
        return member.getPassword();
    }

    @Override
    public String getUsername() {
        // 회원 닉네임 반환
        return member.getNickname();
    }

    @Override
    public boolean isAccountNonExpired() {
        // 계정 만료 여부 (현재 만료된 상태로 설정)
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        // 계정 잠금 여부 (현재 잠긴 상태로 설정)
        return false;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        // 자격 증명 만료 여부 (현재 만료된 상태로 설정)
        return false;
    }

    @Override
    public boolean isEnabled() {
        // 계정 활성화 여부
        return false;
    }
}
