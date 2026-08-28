package br.com.dominus.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
    private static HikariDataSource dataSource() {
        HikariConfig config = new HikariConfig();
        String dbUrl = requiredEnvironment("DB_URL");
        String dbUser = requiredEnvironment("DB_USER");
        String dbPass = requiredSecret("DB_PASS");

        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUser);
        config.setPassword(dbPass);
        config.setMaximumPoolSize(20);
        config.setMinimumIdle(5);
        config.setIdleTimeout(30000);
        config.setPoolName("DominusHikariPool");

        return new HikariDataSource(config);
    }

    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            synchronized (DatabaseConfig.class) {
                if (dataSource == null) {
                    dataSource = dataSource();
                }
            }
        }
        return dataSource.getConnection();
    }

    public static void testConnection() throws SQLException {
        try (Connection conn = getConnection()) {
            System.out.println("[HikariCP] Conexão com PostgreSQL estabelecida com sucesso!");
        }
    }

    private static String requiredEnvironment(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("Variável de ambiente obrigatória não definida: " + name);
        }
        return value;
    }

    private static String requiredSecret(String name) {
        String file = System.getenv(name + "_FILE");
        if (file != null && !file.isBlank()) {
            return readSecret(Path.of(file), name + "_FILE");
        }
        return requiredEnvironment(name);
    }

    static String readSecret(Path path, String source) {
        try {
            String value = Files.readString(path, StandardCharsets.UTF_8).trim();
            if (value.isBlank()) {
                throw new IllegalStateException("Segredo vazio: " + source);
            }
            return value;
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Não foi possível ler o segredo: " + source, exception);
        }
    }
}
