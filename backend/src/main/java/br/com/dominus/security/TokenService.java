package br.com.dominus.security;

import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Set;

@ApplicationScoped
public class TokenService {
    static final long EXPIRES_IN_SECONDS = 8 * 60 * 60;

    public String issue(String email, String role) {
        return Jwt.upn(email)
                .groups(Set.of(role))
                .expiresIn(EXPIRES_IN_SECONDS)
                .sign();
    }
}
