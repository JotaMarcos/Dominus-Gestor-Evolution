package br.com.dominus.controller;

import io.quarkus.security.Authenticated;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

@Path("/api/clientes")
@Produces(MediaType.APPLICATION_JSON)
@Authenticated
public class ClienteController {
    @GET
    public List<Map<String, Object>> listar() {
        return List.of(Map.of("id", 1, "nome_empresarial", "Empresa Dominus LTDA", "cnpj", "12.345.678/0001-90",
                "situacao", "ATIVO"));
    }
}
