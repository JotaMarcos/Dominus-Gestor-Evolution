package br.com.dominus;

import br.com.dominus.config.DatabaseConfig;
import br.com.dominus.controller.AuthController;
import br.com.dominus.controller.ClienteController;
import br.com.dominus.controller.LancamentoController;
import br.com.dominus.controller.RelatorioController;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

public class Main {
    public static void main(String[] args) {
        try {
            int port = 8080;
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

            DatabaseConfig.testConnection();

            // Endpoints da API REST
            server.createContext("/api/auth/login", new AuthController.LoginHandler());
            server.createContext("/api/auth/mfa/toggle", new AuthController.MfaToggleHandler());
            server.createContext("/api/auth/mfa/verify", new AuthController.MfaVerifyHandler());
            server.createContext("/api/clientes", new ClienteController());
            server.createContext("/api/lancamentos", new LancamentoController());
            server.createContext("/api/relatorios/exportar", new RelatorioController());

            server.createContext("/", new StaticFileHandler());

            System.out.println("=========================================================");
            System.out.println("   DOMINUS GESTOR - JAVA 21 WEB SERVER INICIADO          ");
            System.out.println("   Servidor de Alta Performance: http://localhost:" + port);
            System.out.println("=========================================================");

            server.start();
        } catch (Exception e) {
            System.err.println("Falha ao iniciar a aplicação: " + e.getMessage());
            System.exit(1);
        }
    }

    static class StaticFileHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            if (path.equals("/")) {
                path = "/index.html";
            }

            if (path.contains("..") || path.contains("\\") || path.indexOf('\0') >= 0) {
                sendTextResponse(exchange, 400, "400 Bad Request");
                return;
            }

            try (InputStream is = Main.class.getResourceAsStream("/webapp" + path)) {
                if (is == null) {
                    sendTextResponse(exchange, 404, "404 Not Found");
                    return;
                }

                String contentType = "application/octet-stream";
                if (path.endsWith(".html"))
                    contentType = "text/html; charset=UTF-8";
                else if (path.endsWith(".css"))
                    contentType = "text/css; charset=UTF-8";
                else if (path.endsWith(".js"))
                    contentType = "application/javascript; charset=UTF-8";
                else if (path.endsWith(".png"))
                    contentType = "image/png";

                exchange.getResponseHeaders().set("Content-Type", contentType);
                exchange.sendResponseHeaders(200, 0);
                try (OutputStream os = exchange.getResponseBody()) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            }
        }

        private static void sendTextResponse(HttpExchange exchange, int status, String body) throws IOException {
            byte[] response = body.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(status, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }
    }
}
