package br.com.dominus.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ClienteController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String json = "[{\"id\":1,\"nome_empresarial\":\"Empresa Dominus LTDA\",\"cnpj\":\"12.345.678/0001-90\",\"situacao\":\"ATIVO\"}]";
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
