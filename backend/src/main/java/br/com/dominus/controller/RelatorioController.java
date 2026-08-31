package br.com.dominus.controller;

import br.com.dominus.service.ReportService;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.enums.SchemaType;
import org.eclipse.microprofile.openapi.annotations.media.Content;
import org.eclipse.microprofile.openapi.annotations.media.Schema;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import org.jboss.logging.Logger;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.sql.Connection;
import java.util.HashMap;

@Path("/api/relatorios/exportar")
@Authenticated
@Tag(name = "Relatórios", description = "Exportação de relatórios gerenciais (JasperReports + Apache POI)")
@SecurityRequirement(name = "cookieAuth")
public class RelatorioController {
    private static final Logger LOG = Logger.getLogger(RelatorioController.class);

    @Inject
    DataSource dataSource;

    @GET
    @Produces(MediaType.WILDCARD)
    @Operation(summary = "Exportar relatório", description = "Gera e retorna, como arquivo para download, o "
            + "relatório de clientes ou financeiro no formato solicitado. Opcionalmente filtra por um único cliente.")
    @Parameter(name = "nome", description = "Relatório a exportar", schema = @Schema(type = SchemaType.STRING, enumeration = {"relatorio_clientes", "relatorio_financeiro"}))
    @Parameter(name = "formato", description = "Formato de exportação", schema = @Schema(type = SchemaType.STRING, enumeration = {"pdf", "xlsx", "docx", "csv", "txt"}))
    @Parameter(name = "cliente", description = "Filtra o relatório por nome do cliente (opcional)")
    @APIResponse(responseCode = "200", description = "Arquivo do relatório gerado",
            content = @Content(mediaType = MediaType.APPLICATION_OCTET_STREAM))
    @APIResponse(responseCode = "400", description = "Relatório ou formato inválido")
    @APIResponse(responseCode = "500", description = "Falha ao gerar o relatório")
    public Response exportar(@QueryParam("nome") @DefaultValue("relatorio_clientes") String report,
            @QueryParam("formato") @DefaultValue("pdf") String formatName,
            @QueryParam("cliente") @DefaultValue("") String clienteFiltro) {
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
            HashMap<String, Object> parametros = new HashMap<>();
            parametros.put("NOME_CLIENTE", clienteFiltro == null ? "" : clienteFiltro.trim());
            ReportService.exportReport(report, format, parametros, connection, output);
            return Response.ok(output.toByteArray(), mimeType)
                    .header("Content-Disposition", "attachment; filename=" + report + "." + formatName.toLowerCase())
                    .build();
        } catch (Exception exception) {
            LOG.error("Falha ao gerar relatório '" + report + "' no formato '" + formatName + "'", exception);
            return error(Response.Status.INTERNAL_SERVER_ERROR, "Falha ao gerar relatório");
        }
    }

    private static Response error(Response.Status status, String message) {
        return Response.status(status).type(MediaType.APPLICATION_JSON).entity(java.util.Map.of("error", message))
                .build();
    }
}
