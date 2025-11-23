package org.gooserver.dto;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ServerConfigResponse {
    private final String appName;
    private final String version;
    private final String osName;
    private final String javaVersion;
    private final Instant now;
}
