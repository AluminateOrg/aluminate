package com.aluminate.aluminate_organization_backend.controller;

    import com.aluminate.aluminate_organization_backend.dto.login.LoginRequest;
    import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
    import com.aluminate.aluminate_organization_backend.service.auth.AuthService;
    import jakarta.validation.Valid;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;


    @RestController
    @RequestMapping("${api.prefix}/auth")
    public class AuthController {

        private final AuthService authService;

        public AuthController(AuthService authService) {
            this.authService = authService;
        }

        @PostMapping("/login")
        public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
            LoginResponse response = authService.login(request.getEmail(), request.getPassword(),request.getRole());
            return ResponseEntity.ok(response);
        }

    }