//
//package com.doosan.christmas.order.config;
//
//import com.doosan.christmas.order.security.UserDetailsImpl;
//import com.doosan.christmas.order.domain.Member;
//import com.doosan.christmas.order.shared.Role;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.SignatureAlgorithm;
//import io.jsonwebtoken.security.Keys;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.stereotype.Component;
//
//import jakarta.annotation.PostConstruct;
//import java.lang.reflect.Field;
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//import java.util.Base64;
//
//@Slf4j
//@Component("orderJwtTokenProvider")
//@RequiredArgsConstructor
//public class JwtTokenProvider {
//
//    @Value("${jwt.secret}")
//    private String secret;
//
//    private Key key;
//
//    @PostConstruct
//    public void init() {
//        // Base64로 인코딩된 시크릿 키를 디코딩
//        byte[] decodedKey = Base64.getDecoder().decode(secret);
//        // 디코딩된 키를 사용하여 HMAC-SHA 키 생성
//        this.key = Keys.hmacShaKeyFor(decodedKey);
//    }
//
//    public UsernamePasswordAuthenticationToken getAuthentication(String token) {
//        try {
//            Claims claims = Jwts.parserBuilder()
//                    .setSigningKey(key)
//                    .build()
//                    .parseClaimsJws(token)
//                    .getBody();
//
//            Long memberId = claims.get("memberId", Long.class);
//            log.debug("토큰에서 추출한 memberId: {}", memberId);
//
//            // Member 객체 생성 - 최소한의 필수 필드만 설정
//            Member member = Member.builder()
//                    .email("temp@temp.com")
//                    .password("temppass")
//                    .name("temp")
//                    .nickname("temp")
//                    .role(Role.ROLE_USER)
//                    .build();
//
//            // Reflection을 사용하여 id 설정
//            try {
//                Field idField = Member.class.getDeclaredField("id");
//                idField.setAccessible(true);
//                idField.set(member, memberId);
//            } catch (Exception e) {
//                log.error("Member ID 설정 실패: {}", e.getMessage());
//            }
//
//            UserDetailsImpl userDetails = new UserDetailsImpl(member);
//            return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
//        } catch (Exception e) {
//            log.error("토큰 인증 실패: {}", e.getMessage());
//            return null;
//        }
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token);
//            return true;
//        } catch (Exception e) {
//            log.error("토큰 검증 실패: {}", e.getMessage(), e);
//            return false;
//        }
//    }
//}
//
