package com.aluminate.aluminate_organization_backend.controller.fallback;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin")
public class FallbackController {
    // This controller is a fallback for admin-related endpoints.
    // It currently does not have any methods defined.
    // You can add methods for admin functionalities as needed.

    // Example: Add a method to handle fallback for admin operations
    // @GetMapping("/fallback")
    // public ResponseEntity<String> adminFallback() {
    //     return ResponseEntity.ok("Admin fallback endpoint");
    // }
    @GetMapping("/test")
    public String test() {
        return "Test endpoint is working!";
    }
}
