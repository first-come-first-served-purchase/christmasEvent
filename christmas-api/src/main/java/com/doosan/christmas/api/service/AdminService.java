package com.doosan.christmas.api.service;

import com.doosan.christmas.api.dto.member.MemberResponseDto;
import com.doosan.christmas.common.domain.Member;
import com.doosan.christmas.common.exception.CustomException;
import com.doosan.christmas.common.exception.ErrorCode;
import com.doosan.christmas.common.repository.MemberRepository;
import com.doosan.christmas.common.shared.Authority;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Page<MemberResponseDto> getAllMembers(Pageable pageable) {
        return memberRepository.findAll(pageable)
                .map(MemberResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<MemberResponseDto> searchMembers(String keyword) {
        return memberRepository.findByEmailContainingOrNicknameContaining(keyword, keyword)
                .stream()
                .map(MemberResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public void grantAdminRole(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        if (!member.getRoles().contains(Authority.ROLE_ADMIN)) {
            member.getRoles().add(Authority.ROLE_ADMIN);
            memberRepository.save(member);
        }
    }

    @Transactional
    public void revokeAdminRole(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.getRoles().remove(Authority.ROLE_ADMIN);
        memberRepository.save(member);
    }

    @Transactional
    public void blockMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.setBlocked(true);
        memberRepository.save(member);
    }

    @Transactional
    public void unblockMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        
        member.setBlocked(false);
        memberRepository.save(member);
    }
} 