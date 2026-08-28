package br.com.dominus.controller;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.mindrot.jbcrypt.BCrypt;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Map;

@Path("/api/auth")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    @Inject
    DataSource dataSource;

    @Inject
    br.com.dominus.service.MfaService mfaService;

    @POST
    @Path("/login")
    public Response login(JsonNode request) {
        String email = text(request, "email");
        String password = text(request, "senha");
        if (email.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "E-mail e senha são obrigatórios")).build();
        }
        String sql = "SELECT u.senha_hash, u.mfa_habilitado, p.nome FROM usuario u JOIN perfil p ON p.id = u.id_perfil WHERE lower(u.email) = lower(?) AND u.situacao = 'ATIVO'";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !BCrypt.checkpw(password, result.getString("senha_hash"))) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("error", "Credenciais inválidas")).build();
                }
                return Response.ok(Map.of("status", "SUCCESS", "role", result.getString("nome"), "mfaRequired",
                        result.getBoolean("mfa_habilitado"))).build();
            }
        } catch (Exception exception) {
            return unavailable();
        }
    }

    @POST
    @Path("/mfa/toggle")
    public Response toggleMfa() {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(Map.of("error", "Operação requer usuário autenticado")).build();
    }

    @POST
    @Path("/mfa/verify")
    public Response verifyMfa(JsonNode request) {
        String email = text(request, "email");
        String code = text(request, "code");
        if (email.isBlank() || !code.matches("\\d{6}")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "E-mail e código MFA são obrigatórios")).build();
        }
        String sql = "SELECT mfa_secret FROM usuario WHERE lower(email) = lower(?) AND situacao = 'ATIVO' AND mfa_habilitado = TRUE";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getString("mfa_secret") == null
                        || !mfaService.verifyCode(result.getString("mfa_secret"), Integer.parseInt(code))) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Código MFA inválido"))
                            .build();
                }
                return Response.ok(Map.of("status", "VALIDATED")).build();
            }
        } catch (Exception exception) {
            return unavailable();
        }
    }

    private static String text(JsonNode request, String field) {
        JsonNode value = request == null ? null : request.get(field);
        return value == null ? "" : value.asText().trim();
    }

    private static Response unavailable() {
        return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                .entity(Map.of("error", "Serviço de autenticação indisponível")).build();
    }
}
