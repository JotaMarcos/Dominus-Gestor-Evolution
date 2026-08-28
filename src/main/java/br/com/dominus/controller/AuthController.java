package br.com.dominus.controller;

import br.com.dominus.config.DatabaseConfig;
import br.com.dominus.service.MfaService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.mindrot.jbcrypt.BCrypt;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AuthController {
    private static final ObjectMapper JSON = new ObjectMapper();
    private static final MfaService MFA = new MfaService();

    public static class LoginHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }
            JsonNode request = readJson(exchange);
            String email = text(request, "email");
            String password = text(request, "senha");
            if (email.isBlank() || password.isBlank()) {
                sendJsonResponse(exchange, 400, "{\"error\":\"E-mail e senha são obrigatórios\"}");
                return;
            }

            String sql = "SELECT u.senha_hash, u.mfa_habilitado, p.nome "
                    + "FROM usuario u JOIN perfil p ON p.id = u.id_perfil "
                    + "WHERE lower(u.email) = lower(?) AND u.situacao = 'ATIVO'";
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || !BCrypt.checkpw(password, result.getString("senha_hash"))) {
                        sendJsonResponse(exchange, 401, "{\"error\":\"Credenciais inválidas\"}");
                        return;
                    }
                    String role = result.getString("nome");
                    boolean mfaRequired = result.getBoolean("mfa_habilitado");
                    sendJsonResponse(exchange, 200, JSON.writeValueAsString(
                            java.util.Map.of("status", "SUCCESS", "role", role, "mfaRequired", mfaRequired)));
                }
            } catch (Exception exception) {
                sendJsonResponse(exchange, 503, "{\"error\":\"Serviço de autenticação indisponível\"}");
            }
        }
    }

    public static class MfaToggleHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }
            sendJsonResponse(exchange, 501, "{\"error\":\"Operação requer usuário autenticado\"}");
        }
    }

    public static class MfaVerifyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendJsonResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
                return;
            }
            JsonNode request = readJson(exchange);
            String email = text(request, "email");
            String code = text(request, "code");
            if (email.isBlank() || !code.matches("\\d{6}")) {
                sendJsonResponse(exchange, 400, "{\"error\":\"E-mail e código MFA são obrigatórios\"}");
                return;
            }
            String sql = "SELECT mfa_secret FROM usuario WHERE lower(email) = lower(?) "
                    + "AND situacao = 'ATIVO' AND mfa_habilitado = TRUE";
            try (Connection connection = DatabaseConfig.getConnection();
                    PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, email);
                try (ResultSet result = statement.executeQuery()) {
                    if (!result.next() || result.getString("mfa_secret") == null
                            || !MFA.verifyCode(result.getString("mfa_secret"), Integer.parseInt(code))) {
                        sendJsonResponse(exchange, 401, "{\"error\":\"Código MFA inválido\"}");
                        return;
                    }
                    sendJsonResponse(exchange, 200, "{\"status\":\"VALIDATED\"}");
                }
            } catch (Exception exception) {
                sendJsonResponse(exchange, 503, "{\"error\":\"Serviço de autenticação indisponível\"}");
            }
        }
    }

    private static JsonNode readJson(HttpExchange exchange) throws IOException {
        return JSON.readTree(exchange.getRequestBody());
    }

    private static String text(JsonNode request, String field) {
        JsonNode value = request == null ? null : request.get(field);
        return value == null ? "" : value.asText().trim();
    }

    private static void sendJsonResponse(HttpExchange exchange, int code, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
