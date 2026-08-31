package br.com.dominus.controller;

import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Listagem paginada de clientes. A busca por nome usa Full-Text Search nativo
 * do PostgreSQL (to_tsvector/plainto_tsquery) em produção; no perfil dev/test
 * (H2, sem suporte a tsvector) a mesma busca cai para ILIKE, mantendo a API
 * idêntica nos dois ambientes.
 */
@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ClienteController {
    private static final Set<Integer> TAMANHOS_PAGINA = Set.of(10, 20, 50, 100);
    private static final Set<String> COLUNAS_ORDENACAO = Set.of("nome_empresarial", "cnpj");

    @Inject
    DataSource dataSource;

    @ConfigProperty(name = "quarkus.datasource.db-kind")
    String dbKind;

    @GET
    public Response listar(@QueryParam("q") @DefaultValue("") String termo,
            @QueryParam("page") @DefaultValue("1") int pagina,
            @QueryParam("pageSize") @DefaultValue("20") int tamanhoPagina,
            @QueryParam("sort") @DefaultValue("nome_empresarial") String ordenacao,
            @QueryParam("dir") @DefaultValue("asc") String direcao) {
        int paginaValida = Math.max(pagina, 1);
        int tamanhoValido = TAMANHOS_PAGINA.contains(tamanhoPagina) ? tamanhoPagina : 20;
        String colunaValida = COLUNAS_ORDENACAO.contains(ordenacao) ? ordenacao : "nome_empresarial";
        String direcaoValida = "desc".equalsIgnoreCase(direcao) ? "DESC" : "ASC";
        String termoBusca = termo == null ? "" : termo.trim();
        boolean usaFullText = "postgresql".equals(dbKind);

        String filtro = termoBusca.isBlank() ? ""
                : usaFullText
                        ? " WHERE to_tsvector('portuguese', nome_empresarial) @@ plainto_tsquery('portuguese', ?)"
                        : " WHERE UPPER(nome_empresarial) LIKE UPPER(?) ESCAPE '\\'";

        String sqlPagina = "SELECT id, nome_empresarial, cnpj, email, telefone, cidade, estado, situacao FROM cliente"
                + filtro + " ORDER BY " + colunaValida + " " + direcaoValida + ", id ASC LIMIT ? OFFSET ?";
        String sqlTotal = "SELECT COUNT(*) FROM cliente" + filtro;

        try (Connection connection = dataSource.getConnection()) {
            long totalItens = contar(connection, sqlTotal, termoBusca, usaFullText);
            List<Map<String, Object>> itens = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sqlPagina)) {
                int indice = preencherFiltro(statement, 1, termoBusca, usaFullText);
                statement.setInt(indice++, tamanhoValido);
                statement.setInt(indice, (paginaValida - 1) * tamanhoValido);
                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        Map<String, Object> cliente = new LinkedHashMap<>();
                        cliente.put("id", result.getInt("id"));
                        cliente.put("nomeEmpresarial", result.getString("nome_empresarial"));
                        cliente.put("cnpj", result.getString("cnpj"));
                        cliente.put("email", result.getString("email"));
                        cliente.put("telefone", result.getString("telefone"));
                        cliente.put("cidade", result.getString("cidade"));
                        cliente.put("estado", result.getString("estado"));
                        cliente.put("situacao", result.getString("situacao"));
                        itens.add(cliente);
                    }
                }
            }
            long totalPaginas = totalItens == 0 ? 0 : (totalItens + tamanhoValido - 1) / tamanhoValido;

            Map<String, Object> resposta = new LinkedHashMap<>();
            resposta.put("items", itens);
            resposta.put("page", paginaValida);
            resposta.put("pageSize", tamanhoValido);
            resposta.put("totalItems", totalItens);
            resposta.put("totalPages", totalPaginas);
            resposta.put("sort", colunaValida);
            resposta.put("dir", direcaoValida.toLowerCase());
            resposta.put("q", termoBusca);
            return Response.ok(resposta).build();
        } catch (Exception exception) {
            return Response.status(Response.Status.SERVICE_UNAVAILABLE)
                    .entity(Map.of("error", "Não foi possível listar os clientes")).build();
        }
    }

    private long contar(Connection connection, String sqlTotal, String termoBusca, boolean usaFullText)
            throws Exception {
        try (PreparedStatement statement = connection.prepareStatement(sqlTotal)) {
            preencherFiltro(statement, 1, termoBusca, usaFullText);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? result.getLong(1) : 0;
            }
        }
    }

    private int preencherFiltro(PreparedStatement statement, int indice, String termoBusca, boolean usaFullText)
            throws Exception {
        if (termoBusca.isBlank()) {
            return indice;
        }
        if (usaFullText) {
            statement.setString(indice, termoBusca);
        } else {
            String escapado = termoBusca.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
            statement.setString(indice, "%" + escapado + "%");
        }
        return indice + 1;
    }
}
