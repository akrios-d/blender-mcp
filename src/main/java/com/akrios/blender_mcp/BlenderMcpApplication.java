package com.akrios.blender_mcp;

import com.akrios.blender_mcp.net.BlenderConnection;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class BlenderMcpApplication {

	public static void main(String[] args) {
		SpringApplication.run(BlenderMcpApplication.class, args);
	}

    @Bean
    public BlenderConnection blenderConnection() {
        return new BlenderConnection("localhost", 9876);
    }
}
