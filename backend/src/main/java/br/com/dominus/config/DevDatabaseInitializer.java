package br.com.dominus.config;

import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Statement;

@ApplicationScoped
@IfBuildProfile("dev")
public class DevDatabaseInitializer {
    @Inject
    DataSource dataSource;

    void initialize(@Observes StartupEvent event) {
        resetDatabase();
        executeScript("db/schema.sql");
        executeScript("db/dev-seed.sql");
    }

    private void resetDatabase() {
        try (Connection connection = dataSource.getConnection(); Statement statement = connection.createStatement()) {
            statement.execute("DROP ALL OBJECTS");
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao limpar o banco dev", exception);
        }
    }

    private void executeScript(String resource) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (input == null) {
                throw new IllegalStateException("Script não encontrado: " + resource);
            }
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8)
                    .replaceAll("(?i)ON\\s+CONFLICT\\s*\\([^)]*\\)\\s*DO\\s+NOTHING", "");
            try (Connection connection = dataSource.getConnection();
                    Statement statement = connection.createStatement()) {
                for (String sql : script.split(";")) {
                    String command = sql.replaceAll("(?m)^\\s*--.*$", "").trim();
                    if (!command.isBlank()) {
                        statement.execute(command);
                    }
                }
            }
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Falha ao inicializar o banco dev: " + resource, exception);
        } catch (Exception exception) {
            throw new IllegalStateException("Falha ao executar o banco dev: " + resource, exception);
        }
    }
}