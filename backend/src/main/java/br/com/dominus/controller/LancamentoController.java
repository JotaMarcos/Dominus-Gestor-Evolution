package br.com.dominus.controller;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.responses.APIResponse;
import org.eclipse.microprofile.openapi.annotations.security.SecurityRequirement;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@Path("/api/lancamentos")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
@Tag(name = "Lançamentos", description = "Lançamentos financeiros (receitas e despesas)")
@SecurityRequirement(name = "cookieAuth")
public class LancamentoController {
    @GET
    @Operation(summary = "Listar lançamentos", description = "Lista os lançamentos financeiros cadastrados "
            + "(conta, categoria e, opcionalmente, cliente ou fornecedor).")
    @APIResponse(responseCode = "200", description = "Lista de lançamentos financeiros")
    public List<Map<String, Object>> listar() {
        return List
                .of(Map.of("id", 101, "descricao", "Pagamento Licença Software", "valor", 4500.00, "situacao", "PAGO"));
    }
}
