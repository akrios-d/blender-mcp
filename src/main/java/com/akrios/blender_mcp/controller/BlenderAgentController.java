package com.akrios.blender_mcp.controller;

import com.akrios.blender_mcp.net.BlenderConnection;
import com.akrios.blender_mcp.service.OllamaClient;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/agent")
@RequiredArgsConstructor
public class BlenderAgentController {

    private final BlenderConnection blenderConnection;
    private final OllamaClient ollamaClient;

    @PostMapping("/run")
    public String runAgent(@RequestBody Map<String, String> body) {
        try {
            String userPrompt = body.get("prompt");

            // Call LLM and get JSON instructions
            String llmResponse = ollamaClient.query(
                    "You are an assistant that generates Blender MCP commands in JSON format.\n" +
                            "Given the user request: \"" + userPrompt + "\", output a JSON object with a single key \"actions\".\n" +
                            "The value of \"actions\" must be an array of action objects. Each action object has:\n" +
                            "  - type: one of [create_object, modify_object, delete_object, set_material, set_texture, execute_code, render]\n" +
                            "  - params: a JSON object with parameters matching the BlenderMCP Python API:\n" +
                            "      create_object: { type: \"CUBE|SPHERE|CYLINDER|PLANE|CONE|TORUS|EMPTY|CAMERA|LIGHT\", name: \"object_name\", location: [x,y,z], rotation: [x,y,z], scale: [x,y,z] }\n" +
                            "      modify_object: { name: \"object_name\", location: [x,y,z], rotation: [x,y,z], scale: [x,y,z], visible: true|false }\n" +
                            "      delete_object: { name: \"object_name\" }\n" +
                            "      set_material: { object_name: \"object_name\", material_name: \"material_name\", create_if_missing: true|false, color: [r,g,b,(a)] }\n" +
                            "      set_texture: { object_name: \"object_name\", texture_id: \"polyhaven_texture_id\" }\n" +
                            "      execute_code: { code: \"python_code_string\" }\n" +
                            "      render: { output_path: \"path/to/output.png\", resolution_x: int, resolution_y: int }\n" +
                            "Only respond with valid JSON. Do not include explanations, comments, or extra text.\n" +
                            "Example response:\n" +
                            "{\n" +
                            "  \"actions\": [\n" +
                            "    { \"type\": \"create_object\", \"params\": { \"type\": \"CUBE\", \"name\": \"Cube\", \"location\": [0,0,0], \"rotation\": [0,0,0], \"scale\": [1,1,1] } },\n" +
                            "    { \"type\": \"set_material\", \"params\": { \"object_name\": \"Cube\", \"material_name\": \"Metal\", \"create_if_missing\": true, \"color\": [1,0,0] } }\n" +
                            "  ]\n" +
                            "}"
            );

            var objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
            Map<String, Object> jsonResponse = objectMapper.readValue(llmResponse, Map.class);
            List<Map<String, Object>> actions = (List<Map<String, Object>>) jsonResponse.get("actions");

            StringBuilder resultLog = new StringBuilder();

            for (Map<String, Object> action : actions) {
                String type = action.get("type").toString();
                Map<String, Object> params = (Map<String, Object>) action.get("params");

                switch (type) {
                    case "create_object":
                        var createResult = blenderConnection.sendCommand("create_object", params);
                        resultLog.append("Created object: ").append(createResult.get("name")).append("\n");
                        break;
                    case "modify_object":
                        var modifyResult = blenderConnection.sendCommand("modify_object", params);
                        resultLog.append("Modified object: ").append(modifyResult.get("name")).append("\n");
                        break;
                    case "delete_object":
                        blenderConnection.sendCommand("delete_object", params);
                        resultLog.append("Deleted object: ").append(params.get("name")).append("\n");
                        break;
                    case "set_material":
                        var matResult = blenderConnection.sendCommand("set_material", params);
                        resultLog.append("Applied material: ").append(matResult.get("material_name")).append("\n");
                        break;
                    case "set_texture":
                        var texResult = blenderConnection.sendCommand("set_texture", params);
                        resultLog.append("Set texture result: ").append(texResult).append("\n");
                        break;
                    case "execute_code":
                        var codeResult = blenderConnection.sendCommand("execute_code", params);
                        resultLog.append("Executed code: ").append(codeResult.get("result")).append("\n");
                        break;
                    case "render":
                        var renderResult = blenderConnection.sendCommand("render_scene", params);
                        if ((Boolean) renderResult.getOrDefault("rendered", false)) {
                            String filePath = params.getOrDefault("file_path", "render.png").toString();
                            File file = new File(filePath);
                            if (file.exists()) {
                                FileInputStream fis = new FileInputStream(file);
                                byte[] data = fis.readAllBytes();
                                fis.close();
                                String encoded = Base64.encodeBase64String(data);
                                resultLog.append("Rendered image (Base64): ").append(encoded).append("\n");
                            } else {
                                resultLog.append("Rendered file not found: ").append(filePath).append("\n");
                            }
                        } else {
                            resultLog.append("Rendering failed.\n");
                        }
                        break;
                    default:
                        resultLog.append("Unknown action type: ").append(type).append("\n");
                        break;
                }
            }

            return resultLog.toString();

        } catch (Exception e) {
            return "Error in agent execution: " + e.getMessage();
        }
    }
}
