package com.securebank.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class HealthController {

    @GetMapping("public/health")
    public ResponseEntity<String> health(){
        return ResponseEntity.ok("Secure Bank Api is running");
    }
    @GetMapping("private/test")
    public ResponseEntity<String> test(){
        return ResponseEntity.ok("you are authenticated ✅");
    }

}
