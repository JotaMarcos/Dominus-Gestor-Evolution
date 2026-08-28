package br.com.dominus.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api/lancamentos")
@Produces(MediaType.APPLICATION_JSON)
public class LancamentoController {
    @GET
    public List<Map<String, Object>> listar() {
        return List
                .of(Map.of("id", 101, "descricao", "Pagamento Licença Software", "valor", 4500.00, "situacao", "PAGO"));
    }
}
