package com.aluminate.aluminate_organization_backend.controller;

    import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
    import com.aluminate.aluminate_organization_backend.dto.login.LoginRequest;
    import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
    import com.aluminate.aluminate_organization_backend.service.auth.AuthService;
    import jakarta.validation.Valid;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;


    @RestController
    @RequestMapping("${api.prefix}/auth")
    public class AuthController {

        private final AuthService authService;
        private final Logger logger = LoggerFactory.getLogger(AuthController.class);

        public AuthController(AuthService authService) {
            this.authService = authService;
        }

        @PostMapping("/login")
        public ResponseEntity<ResponseWrapper<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
            try{
                LoginResponse response = authService.login(request.getEmail(), request.getPassword());
                ResponseWrapper<LoginResponse> body = new ResponseWrapper<>(true, "Login successful", response);
                return ResponseEntity.ok(body);
            }catch (Exception e){
                logger.error(e.getMessage());
                ResponseWrapper<LoginResponse> body = new ResponseWrapper<>(false, e.getMessage(), null);
                return ResponseEntity.badRequest().body(body);

            }

        }

    }