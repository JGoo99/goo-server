package org.gooserver.controller;

import lombok.AllArgsConstructor;
import org.gooserver.monitoring.RequestMetrics;
import org.gooserver.monitoring.RequestMetrics.ProcessStatusSnapshot;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@AllArgsConstructor
@RequestMapping("/process")
public class ProcessStatusController {

    private final RequestMetrics requestMetrics;

    @GetMapping("/status")
    public ResponseEntity<ProcessStatusSnapshot> getStatus() {
        ProcessStatusSnapshot snapshot = requestMetrics.snapshot();
        return ResponseEntity.ok(snapshot);
    }

    @GetMapping("/err")
    public void throwError() {
        throw new Error("test error");
    }
}
