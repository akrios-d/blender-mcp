package com.akrios.blender_mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OllamaClient {

    private final OllamaChatModel ollama;

    public OllamaClient(OllamaChatModel ollama) {
        this.ollama = ollama;
    }

    /**
     * Query the Ollama chat model with a user prompt
     *
     * @param promptText the user's message
     * @return the assistant's response
     */
    public String query(String promptText) {
        try {
            // Directly call the Ollama model with a string prompt
            return ollama.call(promptText);
        } catch (Exception e) {
            log.error("Error querying Ollama: ", e);
            return "Error querying Ollama: " + e.getMessage();
        }
    }
}
