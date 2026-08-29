package br.com.dominus.security;

import io.quarkus.test.junit.QuarkusTest;
import io.smallrye.jwt.auth.principal.JWTParser;
import jakarta.inject.Inject;
import org.eclipse.microprofile.jwt.JsonWebToken;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
class TokenServiceTest {
    @Inject
    TokenService tokenService;

    @Inject
    JWTParser jwtParser;

    @Test
    void deveEmitirTokenComEmailEPapelDoUsuario() throws Exception {
        String token = tokenService.issue("admin@dominus.com.br", "ADMINISTRADOR");

        JsonWebToken jwt = jwtParser.parse(token);

        assertEquals("admin@dominus.com.br", jwt.getName());
        assertTrue(jwt.getGroups().contains("ADMINISTRADOR"));
    }
}
