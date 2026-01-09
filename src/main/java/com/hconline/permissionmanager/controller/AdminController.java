package com.hconline.permissionmanager.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
public class AdminController {
    
    @GetMapping("/audit")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> getAuditLog() {
        Map<String, String> audit = new HashMap<>();
        audit.put("message", "Audit log access granted");
        audit.put("timestamp", java.time.Instant.now().toString());
        return ResponseEntity.ok(audit);
    }
}
