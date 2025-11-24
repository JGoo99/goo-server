package org.gooserver.monitoring;

import jakarta.annotation.PreDestroy;
import org.gooserver.logging.LogFileService;
import org.gooserver.monitoring.RequestMetrics.ProcessStatusSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ShutdownLogger {

    private static final Logger log = LoggerFactory.getLogger(ShutdownLogger.class);

    private final RequestMetrics requestMetrics;
    private final LogFileService logFileService;

    public ShutdownLogger(RequestMetrics requestMetrics,
                          LogFileService logFileService) {
        this.requestMetrics = requestMetrics;
        this.logFileService = logFileService;
    }

    @PreDestroy
    public void onShutdown() {
        try {
            ProcessStatusSnapshot snapshot = requestMetrics.snapshot();

            String summary = buildSummary(snapshot);

            log.info("Graceful shutdown summary: {}", summary);
            logFileService.writeShutdownEvent(summary);
        } catch (Exception e) {
            log.warn("Failed to log shutdown summary: {}", e.getMessage());
        }
    }

    private String buildSummary(ProcessStatusSnapshot s) {
        String lastErrorType = "-";
        String lastErrorStatus = "-";
        String lastErrorTime = "-";

        if (s.lastError() != null) {
            lastErrorType = s.lastError().type() != null ? s.lastError().type() : "-";
            lastErrorStatus = String.valueOf(s.lastError().status());
            lastErrorTime = s.lastError().occurredAt() != null ? s.lastError().occurredAt() : "-";
        }

        return String.format(
            "uptimeSec=%d totalRequests=%d totalErrors=%d avgDurationMs=%.2f lastErrorType=%s lastErrorStatus=%s lastErrorAt=%s",
            s.uptimeSec(),
            s.totalRequests(),
            s.totalErrors(),
            s.avgDurationMs(),
            lastErrorType,
            lastErrorStatus,
            lastErrorTime
        );
    }
}
