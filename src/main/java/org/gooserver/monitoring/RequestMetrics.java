package org.gooserver.monitoring;

import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class RequestMetrics {

    private final Instant startTime = Instant.now();

    private final AtomicLong totalRequests = new AtomicLong(0);
    private final AtomicLong totalErrors = new AtomicLong(0);
    private final AtomicLong totalDurationMs = new AtomicLong(0);

    private final AtomicReference<LastErrorInfo> lastErrorInfo =
        new AtomicReference<>(null);

    public void recordRequest(long durationMs, int status, Throwable error) {
        totalRequests.incrementAndGet();
        totalDurationMs.addAndGet(durationMs);

        if (status >= 500 || error != null) {
            totalErrors.incrementAndGet();
            LastErrorInfo info = new LastErrorInfo(
                OffsetDateTime.now(ZoneOffset.systemDefault()).toString(),
                (error != null ? error.getClass().getSimpleName() : null),
                (error != null ? safeMessage(error.getMessage()) : null),
                status
            );
            lastErrorInfo.set(info);
        }
    }

    private String safeMessage(String msg) {
        if (msg == null) {
            return null;
        }
        if (msg.length() > 200) {
            return msg.substring(0, 200) + "...";
        }
        return msg;
    }

    public ProcessStatusSnapshot snapshot() {
        Instant now = Instant.now();
        long uptimeSec = Duration.between(startTime, now).getSeconds();

        long totalReq = totalRequests.get();
        long totalErr = totalErrors.get();
        long totalDur = totalDurationMs.get();
        double avgMs = (totalReq > 0) ? (double) totalDur / totalReq : 0.0;

        LastErrorInfo last = lastErrorInfo.get();

        return new ProcessStatusSnapshot(
            startTime.toString(),
            uptimeSec,
            totalReq,
            totalErr,
            avgMs,
            last
        );
    }

    public record LastErrorInfo(String occurredAt, String type, String message, int status) {
    }

    public record ProcessStatusSnapshot(String startTime, long uptimeSec, long totalRequests, long totalErrors,
                                        double avgDurationMs, LastErrorInfo lastError) {
    }
}
