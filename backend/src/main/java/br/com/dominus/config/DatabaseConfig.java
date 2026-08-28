package br.com.dominus.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

public class DatabaseConfig {
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
