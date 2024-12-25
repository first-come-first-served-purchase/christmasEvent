package com.doosan.christmas.service;

import com.doosan.christmas.domain.Member;
import com.doosan.christmas.repository.MemberRepository;
import com.doosan.christmas.domain.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final MemberRepository memberRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        // 닉네임으로 회원 정보 조회
        Optional<Member> member = memberRepository.findByNickname(nickname);

        // 조회된 회원 정보를 UserDetailsImpl로 반환
        return member
                .map(UserDetailsImpl::new) // UserDetailsImpl 객체로 매핑
                .orElseThrow(() -> new UsernameNotFoundException("이메일 혹은 비밀번호가 일치하지 않습니다."));
                // 회원 정보가 없으면 예외 발생
    }
}