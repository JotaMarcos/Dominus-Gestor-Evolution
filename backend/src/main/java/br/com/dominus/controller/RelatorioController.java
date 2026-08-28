package br.com.dominus.controller;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import br.com.dominus.config.DatabaseConfig;
import br.com.dominus.service.ReportService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

public class RelatorioController implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJsonResponse(exchange, 405, "{\"error\":\"Método não permitido\"}");
            return;
        }

        String report = "relatorio_clientes";
        String fmt = "pdf";

        String query = exchange.getRequestURI().getRawQuery();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=", 2);
                if (pair.length < 2) {
                    continue;
                }
                String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                String value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                if ("nome".equals(key)) {
                    report = value;
                } else if ("formato".equals(key)) {
                    fmt = value.toLowerCase();
                }
            }
        }

        if (!"relatorio_clientes".equals(report) && !"relatorio_financeiro".equals(report)) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Relatório inválido\"}");
            return;
        }

        ReportService.Format format = switch (fmt) {
            case "xlsx" -> ReportService.Format.XLSX;
            case "csv" -> ReportService.Format.CSV;
            case "docx" -> ReportService.Format.DOCX;
            case "txt" -> ReportService.Format.TXT;
            case "pdf" -> ReportService.Format.PDF;
            default -> null;
        };
        if (format == null) {
            sendJsonResponse(exchange, 400, "{\"error\":\"Formato inválido\"}");
            return;
        }

        String mimeType = switch (format) {
            case PDF -> "application/pdf";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV -> "text/csv";
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case TXT -> "text/plain";
        };

        try (Connection conn = DatabaseConfig.getConnection();
                ByteArrayOutputStream reportOutput = new ByteArrayOutputStream()) {
            Map<String, Object> params = new HashMap<>();
            ReportService.exportReport(report, format, params, conn, reportOutput);
            byte[] response = reportOutput.toByteArray();
            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=" + report + "." + fmt);
            exchange.sendResponseHeaders(200, response.length);
            try (var os = exchange.getResponseBody()) {
                os.write(response);
            }
        } catch (Exception e) {
            sendJsonResponse(exchange, 500, "{\"error\":\"Falha ao gerar relatório\"}");
        }
    }

    private static void sendJsonResponse(HttpExchange exchange, int status, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8");
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}
