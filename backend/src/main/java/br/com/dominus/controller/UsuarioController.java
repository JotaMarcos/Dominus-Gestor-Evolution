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
import java.util.Map;

@Path("/api/usuarios")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class UsuarioController {
    @Inject
    DataSource dataSource;

    @POST
    public Response cadastrar(JsonNode request) {
        String nome = text(request, "nome");
        String login = text(request, "login");
        String email = text(request, "email");
        String senha = text(request, "senha");
        if (nome.isBlank() || login.isBlank() || email.isBlank() || senha.length() < 8) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "Nome, login, e-mail e senha com no mínimo 8 caracteres são obrigatórios"))
                    .build();
        }
        String sql = "INSERT INTO usuario (nome, login, email, senha_hash, id_perfil, mfa_habilitado, situacao) "
                + "VALUES (?, ?, ?, ?, (SELECT id FROM perfil WHERE nome = 'OPERADOR'), FALSE, 'ATIVO')";
        try (Connection connection = dataSource.getConnection();
                PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, nome);
            statement.setString(2, login);
            statement.setString(3, email);
            statement.setString(4, BCrypt.hashpw(senha, BCrypt.gensalt(12)));
            statement.executeUpdate();
            return Response.status(Response.Status.CREATED).entity(Map.of("status", "CREATED", "perfil", "OPERADOR"))
                    .build();
        } catch (Exception exception) {
            return Response.status(Response.Status.CONFLICT)
                    .entity(Map.of("error", "Login ou e-mail já cadastrado")).build();
        }
    }

    private static String text(JsonNode request, String field) {
        JsonNode value = request == null ? null : request.get(field);
        return value == null ? "" : value.asText().trim();
    }
}