package br.com.dominus.security;

import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SessionCookieFactory {
    static final String COOKIE_NAME = "dominus_session";
    private static final int MAX_AGE_SECONDS = (int) TokenService.EXPIRES_IN_SECONDS;

    @ConfigProperty(name = "dominus.cookie.secure", defaultValue = "false")
    boolean secure;

    public String issue(String token) {
        return build(token, MAX_AGE_SECONDS);
    }

    public String clear() {
        return build("", 0);
    }

    private String build(String value, int maxAgeSeconds) {
        StringBuilder cookie = new StringBuilder()
                .append(COOKIE_NAME).append('=').append(value)
                .append("; Path=/; HttpOnly; SameSite=Strict; Max-Age=").append(maxAgeSeconds);
        if (secure) {
            cookie.append("; Secure");
        }
        return cookie.toString();
    }
}
