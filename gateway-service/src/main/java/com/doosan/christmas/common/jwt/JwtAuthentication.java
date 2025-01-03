package com.doosan.christmas.common.jwt;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthentication extends AbstractAuthenticationToken {
    private final Object principal;
    private final Long id;

    public JwtAuthentication(Object principal, Long id, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.principal = principal;
        this.id = id;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return this.principal;
    }
} 