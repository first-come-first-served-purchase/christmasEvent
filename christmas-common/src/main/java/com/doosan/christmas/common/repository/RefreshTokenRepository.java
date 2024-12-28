package com.doosan.christmas.common.repository;

import com.doosan.christmas.common.domain.Member;
import com.doosan.christmas.common.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    @Query("SELECT r FROM RefreshToken r WHERE r.member = :member")
    Optional<RefreshToken> findByMember(@Param("member") Member member);

    @Query("SELECT r FROM RefreshToken r WHERE r.value = :value")
    Optional<RefreshToken> findByValue(@Param("value") String value);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
} 