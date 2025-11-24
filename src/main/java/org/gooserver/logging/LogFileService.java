package org.gooserver.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class LogFileService {

    private static final Logger log = LoggerFactory.getLogger(LogFileService.class);

    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE_PREFIX = "app-";
    private static final String LOG_FILE_SUFFIX = ".log";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    public void writeLine(String line) {
        try {
            Path logDir = Paths.get(LOG_DIR);
            if (!Files.exists(logDir)) {
                Files.createDirectories(logDir);
            }

            String today = LocalDate.now().format(DATE_FORMAT);
            String fileName = LOG_FILE_PREFIX + today + LOG_FILE_SUFFIX;
            Path logFile = logDir.resolve(fileName);

            try (BufferedWriter writer = Files.newBufferedWriter(
                logFile,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            )) {
                String timestamp = OffsetDateTime.now().format(TS_FORMAT);
                writer.write("[" + timestamp + "] " + line);
                writer.newLine();
            }
        } catch (IOException e) {
            log.warn("Failed to write log file: {}", e.getMessage());
        }
    }
}
