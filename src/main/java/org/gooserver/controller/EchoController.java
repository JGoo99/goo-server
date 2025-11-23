package org.gooserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EchoController {

    @PostMapping("/echo")
    public ResponseEntity<Object> echo(@RequestBody(required = false) Object body) {
        return ResponseEntity.ok(body);
    }
}
