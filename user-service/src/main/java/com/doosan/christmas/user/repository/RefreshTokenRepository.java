package com.doosan.christmas.user.repository;

import com.doosan.christmas.user.domain.Member;
import com.doosan.christmas.user.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByMember(Member member);
    Optional<RefreshToken> findByTokenValue(String tokenValue);
    
    @Transactional
    @Modifying
    void deleteByMemberId(Long memberId);
} 