package br.com.dominus.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class LancamentoController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String json = "[{\"id\":101,\"descricao\":\"Pagamento Licença Software\",\"valor\":4500.00,\"situacao\":\"PAGO\"}]";
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(response);
        }
    }
}
