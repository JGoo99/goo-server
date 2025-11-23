package org.gooserver.config;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@EnableConfigurationProperties(AppConfigProperties.class)
public class AppConfig {
    private static final Logger log = LoggerFactory.getLogger(AppConfig.class);

    private final Environment environment;
    private final AppConfigProperties appConfigProperties;

    public AppConfig(Environment environment, AppConfigProperties appConfigProperties) {
        this.environment = environment;
        this.appConfigProperties = appConfigProperties;
    }

    @PostConstruct
    public void logConfig() {
        String serverPort = environment.getProperty("server.port", "8080");
        log.info("Server starting on port: {}", serverPort);
        log.info("App version: {}", appConfigProperties.getVersion());

        if ("8080".equals(serverPort)) {
            log.warn("No explicit server.port configured. Using default port 8080.");
        }
    }
}
