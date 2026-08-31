package br.com.dominus.controller;

import br.com.dominus.security.SessionCookieFactory;
import br.com.dominus.security.TokenService;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.security.PermitAll;
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
@Produces(MediaType.APPLICATION_JSON)
public class AuthController {
    @Inject
    DataSource dataSource;

    @Inject
    br.com.dominus.service.MfaService mfaService;

    @Inject
    TokenService tokenService;

    @Inject
    SessionCookieFactory sessionCookies;

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response login(JsonNode request) {
        String email = text(request, "email");
        String login = text(request, "login");
        String password = text(request, "senha");
        String identity = login.isBlank() ? email : login;
        if (identity.isBlank() || password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "E-mail e senha são obrigatórios")).build();
        }
        String sql = "SELECT u.email, u.senha_hash, u.mfa_habilitado, p.nome FROM usuario u JOIN perfil p ON p.id = u.id_perfil "
                + "WHERE (lower(u.email) = lower(?) OR lower(u.login) = lower(?)) AND u.situacao = 'ATIVO'";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identity);
            statement.setString(2, identity);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || !BCrypt.checkpw(password, result.getString("senha_hash"))) {
                    return Response.status(Response.Status.UNAUTHORIZED)
                            .entity(Map.of("error", "Credenciais inválidas")).build();
                }
                String role = result.getString("nome");
                boolean mfaRequired = result.getBoolean("mfa_habilitado");
                if (mfaRequired) {
                    return Response.ok(Map.of("status", "SUCCESS", "role", role, "mfaRequired", true)).build();
                }
                String token = tokenService.issue(result.getString("email"), role);
                return Response.ok(Map.of("status", "SUCCESS", "role", role, "mfaRequired", false))
                        .header("Set-Cookie", sessionCookies.issue(token))
                        .build();
            }
        } catch (Exception exception) {
            return unavailable();
        }
    }

    @POST
    @Path("/mfa/toggle")
    @PermitAll
    public Response toggleMfa() {
        return Response.status(Response.Status.NOT_IMPLEMENTED)
                .entity(Map.of("error", "Operação requer usuário autenticado")).build();
    }

    @POST
    @Path("/mfa/verify")
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response verifyMfa(JsonNode request) {
        String email = text(request, "email");
        String code = text(request, "code");
        if (email.isBlank() || !code.matches("\\d{6}")) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "E-mail e código MFA são obrigatórios")).build();
        }
        String sql = "SELECT u.mfa_secret, p.nome FROM usuario u JOIN perfil p ON p.id = u.id_perfil "
                + "WHERE lower(u.email) = lower(?) AND u.situacao = 'ATIVO' AND u.mfa_habilitado = TRUE";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next() || result.getString("mfa_secret") == null
                        || !mfaService.verifyCode(result.getString("mfa_secret"), Integer.parseInt(code))) {
                    return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "Código MFA inválido"))
                            .build();
                }
                String token = tokenService.issue(email, result.getString("nome"));
                return Response.ok(Map.of("status", "VALIDATED"))
                        .header("Set-Cookie", sessionCookies.issue(token))
                        .build();
            }
        } catch (Exception exception) {
            return unavailable();
        }
    }

    @POST
    @Path("/password/forgot")
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response esqueciSenha(JsonNode request) {
        String identity = identidade(request);
        if (identity.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Informe o login ou e-mail cadastrado")).build();
        }
        String sql = "SELECT mfa_habilitado FROM usuario WHERE (lower(email) = lower(?) OR lower(login) = lower(?)) "
                + "AND situacao = 'ATIVO'";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, identity);
            statement.setString(2, identity);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Response.status(Response.Status.NOT_FOUND)
                            .entity(Map.of("error", "Usuário não encontrado ou inativo")).build();
                }
                return Response.ok(Map.of("mfaRequired", result.getBoolean("mfa_habilitado"))).build();
            }
        } catch (Exception exception) {
            return unavailable();
        }
    }

    @POST
    @Path("/password/reset")
    @Consumes(MediaType.APPLICATION_JSON)
    @PermitAll
    public Response redefinirSenha(JsonNode request) {
        String identity = identidade(request);
        String code = text(request, "code");
        String novaSenha = text(request, "novaSenha");
        if (identity.isBlank() || novaSenha.length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Login/e-mail e nova senha com no mínimo 8 caracteres são obrigatórios"))
                    .build();
        }
        String selectSql = "SELECT id, mfa_habilitado, mfa_secret FROM usuario "
                + "WHERE (lower(email) = lower(?) OR lower(login) = lower(?)) AND situacao = 'ATIVO'";
        try (Connection connection = dataSource.getConnection()) {
            int id;
            try (PreparedStatement statement = connection.prepareStatement(selectSql)) {
                statement.setString(1, identity);
                statement.setString(2, identity);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next()) {
                        return Response.status(Response.Status.NOT_FOUND)
                                .entity(Map.of("error", "Usuário não encontrado ou inativo")).build();
                    }
                    if (result.getBoolean("mfa_habilitado")) {
                        if (!code.matches("\\d{6}") || result.getString("mfa_secret") == null
                                || !mfaService.verifyCode(result.getString("mfa_secret"), Integer.parseInt(code))) {
                            return Response.status(Response.Status.UNAUTHORIZED)
                                    .entity(Map.of("error", "Código MFA inválido")).build();
                        }
                    }
                    id = result.getInt("id");
                }
            }
            try (PreparedStatement update = connection
                    .prepareStatement("UPDATE usuario SET senha_hash = ? WHERE id = ?")) {
                update.setString(1, BCrypt.hashpw(novaSenha, BCrypt.gensalt(12)));
                update.setInt(2, id);
                update.executeUpdate();
            }
            return Response.ok(Map.of("status", "SENHA_ATUALIZADA")).build();
        } catch (Exception exception) {
            return unavailable();
        }
    }

    @POST
    @Path("/logout")
    @PermitAll
    public Response logout() {
        return Response.ok(Map.of("status", "LOGGED_OUT"))
                .header("Set-Cookie", sessionCookies.clear())
                .build();
    }

    private static String identidade(JsonNode request) {
        String login = text(request, "login");
        String email = text(request, "email");
        return login.isBlank() ? email : login;
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
