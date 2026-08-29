package br.com.dominus.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.HashMap;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ReportServiceTest {
    private static final String NOME_ACENTUADO = "Confecções São João Ltda";

    private Connection connection;

    @BeforeEach
    void criarBancoComClienteAcentuado() throws Exception {
        String dbName = "report_test_" + UUID.randomUUID();
        connection = DriverManager.getConnection(
                "jdbc:h2:mem:" + dbName + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1", "sa", "");
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE cliente (id INT, nome_empresarial VARCHAR(255), cnpj VARCHAR(20), "
                    + "email VARCHAR(255), telefone VARCHAR(20), situacao VARCHAR(10))");
            statement.execute("INSERT INTO cliente VALUES (1, '" + NOME_ACENTUADO + "', '00.000.000/0001-00', "
                    + "'contato@saojoao.com.br', '11999999999', 'ATIVO')");
        }
    }

    @AfterEach
    void fecharConexao() throws Exception {
        connection.close();
    }

    @Test
    void csvDeveManterAcentuacaoPortuguesaEmUtf8() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ReportService.exportReport("relatorio_clientes", ReportService.Format.CSV, new HashMap<>(), connection,
                output);

        String csv = output.toString(StandardCharsets.UTF_8);

        assertTrue(csv.contains(NOME_ACENTUADO), "CSV deveria conter o nome com acentuação intacta: " + csv);
        assertTrue(csv.contains("Situação"), "Cabeçalho 'Situação' deveria estar presente sem corrupção: " + csv);
    }

    @Test
    void txtDeveManterAcentuacaoPortuguesaEmUtf8() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ReportService.exportReport("relatorio_clientes", ReportService.Format.TXT, new HashMap<>(), connection,
                output);

        String txt = output.toString(StandardCharsets.UTF_8);

        assertTrue(txt.contains(NOME_ACENTUADO), "TXT deveria conter o nome com acentuação intacta: " + txt);
    }

    @Test
    void pdfDeveSerGeradoComCabecalhosEmPortugues() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ReportService.exportReport("relatorio_clientes", ReportService.Format.PDF, new HashMap<>(), connection,
                output);

        byte[] pdf = output.toByteArray();
        assertTrue(pdf.length > 0, "PDF gerado não deveria estar vazio");
        assertTrue(new String(pdf, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-"),
                "Conteúdo deveria começar com a assinatura de um PDF válido");
    }
}
