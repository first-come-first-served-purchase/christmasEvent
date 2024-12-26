package com.doosan.christmas.member.repository;

import com.doosan.christmas.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // ID로 Member 조회
    @Query("SELECT m FROM Member m WHERE m.id = :id")
    Optional<Member> findById(@Param("id") Long id);

    // Email로 Member 조회
    @Query("SELECT m FROM Member m WHERE m.email = :email")
    Optional<Member> findByEmail(@Param("email") String email);

    // Nickname으로 Member 조회
    @Query("SELECT m FROM Member m WHERE m.nickname = :nickname")
    Optional<Member> findByNickname(@Param("nickname") String nickname);
}