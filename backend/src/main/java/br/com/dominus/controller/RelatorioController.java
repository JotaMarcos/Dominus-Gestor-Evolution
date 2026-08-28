package br.com.dominus.controller;

import br.com.dominus.service.ReportService;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.HashMap;

@Path("/api/relatorios/exportar")
public class RelatorioController {
    @Inject
    DataSource dataSource;

    @GET
    @Produces(MediaType.WILDCARD)
    public Response exportar(@QueryParam("nome") @DefaultValue("relatorio_clientes") String report,
            @QueryParam("formato") @DefaultValue("pdf") String formatName) {
        if (!"relatorio_clientes".equals(report) && !"relatorio_financeiro".equals(report)) {
            return error(Response.Status.BAD_REQUEST, "Relatório inválido");
        }
        ReportService.Format format = switch (formatName.toLowerCase()) {
            case "xlsx" -> ReportService.Format.XLSX;
            case "csv" -> ReportService.Format.CSV;
            case "docx" -> ReportService.Format.DOCX;
            case "txt" -> ReportService.Format.TXT;
            case "pdf" -> ReportService.Format.PDF;
            default -> null;
        };
        if (format == null) {
            return error(Response.Status.BAD_REQUEST, "Formato inválido");
        }
        String mimeType = switch (format) {
            case PDF -> "application/pdf";
            case XLSX -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case CSV -> "text/csv";
            case DOCX -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case TXT -> "text/plain";
        };
        try (Connection connection = dataSource.getConnection();
                ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            ReportService.exportReport(report, format, new HashMap<>(), connection, output);
            return Response.ok(output.toByteArray(), mimeType)
                    .header("Content-Disposition", "attachment; filename=" + report + "." + formatName.toLowerCase())
                    .build();
        } catch (Exception exception) {
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Falha ao gerar relatório");
        }
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(java.util.Map.of("error", message))
                .build();
    }
}
