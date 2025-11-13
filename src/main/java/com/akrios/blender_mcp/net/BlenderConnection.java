package com.akrios.blender_mcp.net;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Slf4j
@Getter
public class BlenderConnection {
    private final String host;
    private final int port;
    private Socket socket;
    private final int timeoutMs = 15000; // 15 sec
    private final ObjectMapper mapper = new ObjectMapper();

    public BlenderConnection(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public synchronized void connect() throws IOException {
        if (socket != null && socket.isConnected()) return;
        socket = new Socket(host, port);
        socket.setSoTimeout(timeoutMs);
        log.info("Connected to Blender at {}:{}", host, port);
    }

    public synchronized void disconnect() {
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                log.error("Error disconnecting Blender: {}", e.getMessage());
            } finally {
                socket = null;
            }
        }
    }

    public synchronized Map<String, Object> sendCommand(String commandType, Map<String, Object> params) throws IOException {
        if (socket == null || socket.isClosed()) connect();

        // Build command
        Map<String, Object> command = Map.of(
                "type", commandType,
                "params", params != null ? params : Map.of()
        );

        // Convert command to JSON and send
        String jsonCommand = mapper.writeValueAsString(command);
        OutputStream os = socket.getOutputStream();
        os.write(jsonCommand.getBytes(StandardCharsets.UTF_8));
        os.flush();

        // Read response
        InputStream is = socket.getInputStream();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        long startTime = System.currentTimeMillis();
        int timeoutMs = 5000; // 5 seconds timeout

        while (true) {
            // Check for timeout
            if (System.currentTimeMillis() - startTime > timeoutMs) {
                throw new IOException("Timeout waiting for Blender response");
            }

            // Read available bytes
            while (is.available() > 0) {
                int read = is.read(buffer);
                if (read == -1) throw new IOException("Blender closed the connection unexpectedly");
                baos.write(buffer, 0, read);
            }

            // Try parsing accumulated bytes as JSON
            try {
                String jsonCandidate = baos.toString(StandardCharsets.UTF_8);
                Map<String, Object> response = mapper.readValue(jsonCandidate, Map.class);
                return (Map<String, Object>) response.get("result");
            } catch (com.fasterxml.jackson.core.JsonParseException e) {
                // JSON incomplete, continue reading
                try {
                    Thread.sleep(10); // small delay to avoid busy loop
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IOException("Interrupted while reading Blender response", ie);
                }
            }
        }
    }
}
