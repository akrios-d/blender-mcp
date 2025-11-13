package com.akrios.blender_mcp.controller;

import com.akrios.blender_mcp.net.BlenderConnection;
import com.akrios.blender_mcp.service.OllamaClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class BlenderController {

    private final BlenderConnection blenderConnection;
    private final OllamaClient ollamaClient;

    @GetMapping("/scene")
    public Map<String, Object> getScene() throws Exception {
        return blenderConnection.sendCommand("get_scene_info", null);
    }

    @PostMapping("/object/create")
    public String createObject(@RequestBody Map<String, Object> body) throws Exception {
        var result = blenderConnection.sendCommand("create_object", body);
        return "Created object: " + result.get("name");
    }

    @PostMapping("/object/modify")
    public String modifyObject(@RequestBody Map<String, Object> body) throws Exception {
        var result = blenderConnection.sendCommand("modify_object", body);
        return "Modified object: " + result.get("name");
    }

    @PostMapping("/object/delete")
    public String deleteObject(@RequestBody Map<String, String> body) throws Exception {
        String name = body.get("name");
        blenderConnection.sendCommand("delete_object", Map.of("name", name));
        return "Deleted object: " + name;
    }

    @PostMapping("/ollama/query")
    public String queryOllama(@RequestBody Map<String, String> body) {
        String prompt = body.get("prompt");
        return ollamaClient.query(prompt);  // safe now in blocking context
    }

    @PostMapping("/render")
    public String render(@RequestBody Map<String, String> body) throws Exception {
        String filePath = body.getOrDefault("file_path", "render.png");
        var result = blenderConnection.sendCommand("render_scene", Map.of("output_path", filePath));

        if (result.get("rendered") != null && (Boolean) result.get("rendered")) {
            File file = new File(filePath);
            if (!file.exists()) return "Error: Rendered file not found.";
            FileInputStream fis = new FileInputStream(file);
            byte[] data = fis.readAllBytes();
            fis.close();
            return Base64.encodeBase64String(data);
        } else {
            return "Rendering failed.";
        }
    }
}
