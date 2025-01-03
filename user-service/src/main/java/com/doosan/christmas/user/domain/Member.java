package com.doosan.christmas.user.domain;

import com.doosan.christmas.user.domain.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends Timestamped {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "nickname", nullable = false)
    private String nickname;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address", nullable = false)
    private String address;

    @Column(name = "email_verified", nullable = false)
    private boolean emailVerified = false;

    @Column(name = "is_blocked", nullable = false)
    private boolean isBlocked = false;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Builder
    public Member(String name, String nickname, String email, String password, 
                 String phone, String address, Role role) {
        this.name = name;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.phone = phone;
        this.address = address;
        this.role = role;
        this.emailVerified = false;
        this.isBlocked = false;
        this.isDeleted = false;
    }

    public void verifyEmail() {
        this.emailVerified = true;
    }



    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public boolean matchPassword(PasswordEncoder passwordEncoder, String password) {
        return passwordEncoder.matches(password, this.password);
    }

    public Role getRole() {
        return this.role;
    }
}