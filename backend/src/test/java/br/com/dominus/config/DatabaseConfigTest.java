package br.com.dominus.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

class DatabaseConfigTest {
    @Test
    void deveLerSegredoDoArquivoSemEspacosExternos() throws Exception {
        Path secretFile = Files.createTempFile("postgres-password", ".txt");
        Files.writeString(secretFile, "  segredo-de-teste  \n");

        assertEquals("segredo-de-teste", DatabaseConfig.readSecret(secretFile, "TEST_SECRET"));

        Files.deleteIfExists(secretFile);
    }

    @Test
    void deveRejeitarSegredoVazio() throws Exception {
        Path secretFile = Files.createTempFile("postgres-password-empty", ".txt");
        Files.writeString(secretFile, "  \n");

        assertThrows(IllegalStateException.class, () -> DatabaseConfig.readSecret(secretFile, "TEST_SECRET"));

        Files.deleteIfExists(secretFile);
    }
}