package org.gooserver.controller;

import java.time.Instant;
import lombok.AllArgsConstructor;
import org.gooserver.config.AppConfigProperties;
import org.gooserver.dto.ServerConfigResponse;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
public class ServerConfigController {

    private final Environment environment;
    private final AppConfigProperties appConfigProperties;

    @GetMapping("/config")
    public ResponseEntity<ServerConfigResponse> info() {
        String appName = environment.getProperty("spring.application.name", "local-server");
        String version = appConfigProperties.getVersion();
        String osName = System.getProperty("os.name", "unknown");
        String javaVersion = System.getProperty("java.version", "unknown");

        ServerConfigResponse response = new ServerConfigResponse(
            appName,
            version,
            osName,
            javaVersion,
            Instant.now()
        );

        return ResponseEntity.ok(response);
    }
}
