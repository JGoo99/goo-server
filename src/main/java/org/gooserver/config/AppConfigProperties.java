package org.gooserver.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public class AppConfigProperties {
    private String version = "dev";

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = (version == null || version.isBlank()) ? "dev" : version;
    }
}
