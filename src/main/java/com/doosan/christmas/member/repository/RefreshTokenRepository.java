package com.doosan.christmas.member.repository;

import com.doosan.christmas.member.domain.Member;
import com.doosan.christmas.member.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

// RefreshToken 관련 데이터베이스 작업 처리
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    // 특정 멤버의 RefreshToken 조회
    @Query("SELECT r FROM RefreshToken r WHERE r.member = :member")
    Optional<RefreshToken> findByMember(@Param("member") Member member);

    // 토큰 값으로 RefreshToken 조회
    @Query("SELECT r FROM RefreshToken r WHERE r.value = :value")
    Optional<RefreshToken> findByValue(@Param("value") String value);

    // 멤버 ID로 RefreshToken 삭제
    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.member.id = :memberId")
    void deleteByMemberId(@Param("memberId") Long memberId);
}
