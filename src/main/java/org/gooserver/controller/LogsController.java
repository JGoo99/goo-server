package org.gooserver.controller;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.gooserver.logging.LogFileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/logs")
public class LogsController {

    private final LogFileService logFileService;

    public LogsController(LogFileService logFileService) {
        this.logFileService = logFileService;
    }

    @GetMapping("/recent")
    public ResponseEntity<Map<String, Object>> getRecentLogs(
        @RequestParam(name = "lines", defaultValue = "100") int lines
    ) {
        if (lines <= 0) {
            lines = 100;
        }
        if (lines > 1000) {
            lines = 1000;
        }

        try {
            List<String> recentLines = logFileService.readRecentLines(lines);
            return ResponseEntity.ok(
                Map.of(
                    "lines", recentLines.size(),
                    "content", recentLines
                )
            );
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(
                Map.of(
                    "error", "failed_to_read_log_file",
                    "message", e.getMessage()
                )
            );
        }
    }
}
