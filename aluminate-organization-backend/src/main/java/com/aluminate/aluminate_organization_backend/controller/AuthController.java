package com.aluminate.aluminate_organization_backend.controller;

    import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
    import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
    import com.aluminate.aluminate_organization_backend.dto.login.EncryptedLoginRequest;
    import com.aluminate.aluminate_organization_backend.dto.login.LoginRequest;
    import com.aluminate.aluminate_organization_backend.dto.login.LoginResponse;
    import com.aluminate.aluminate_organization_backend.service.auth.AuthService;
    import com.fasterxml.jackson.databind.ObjectMapper;
    import jakarta.annotation.PostConstruct;
    import jakarta.servlet.http.HttpServletResponse;
    import jakarta.validation.Valid;
    import org.slf4j.Logger;
    import org.slf4j.LoggerFactory;
    import org.springframework.beans.factory.annotation.Value;
    import org.springframework.http.ResponseCookie;
    import org.springframework.http.ResponseEntity;
    import org.springframework.transaction.annotation.Transactional;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RestController;

    import java.security.PrivateKey;


@RestController
    @RequestMapping("${api.prefix}/auth")
    public class AuthController {

        private final AuthService authService;
        private final Logger logger = LoggerFactory.getLogger(AuthController.class);

        @Value("${encryption.organization.private-key}")
        private String organizationPrivateKeyENV;
        private final ObjectMapper objectMapper = new ObjectMapper();

        private PrivateKey organizationPrivateKey;

        @PostConstruct
        public void initKeys() throws Exception {
            this.organizationPrivateKey = RSAEncryptionUtil.privateKeyFromPem(organizationPrivateKeyENV);
        }


        public AuthController(AuthService authService) {
            this.authService = authService;
        }

    @PostMapping("/login")
    public ResponseEntity<ResponseWrapper<LoginResponse>> login(@Valid @RequestBody EncryptedLoginRequest encryptedRequest, HttpServletResponse httpResponse) {
        try{
            logger.info("Login attempting...");
            logger.info("encryptedRequest: " + encryptedRequest);

            //decrypt the request
            String decrypted = RSAEncryptionUtil.decrypt(
                    encryptedRequest.getPayload(),
                    organizationPrivateKey
            );
            logger.info("decrypted: " + decrypted);
            LoginRequest request = objectMapper.readValue(
                    decrypted,
                    LoginRequest.class
            );


            LoginResponse response = authService.login(request.getEmail(), request.getPassword());
            //set cookies- jwt,csrf,session
            authService.setAuthCookies(httpResponse, response.getToken());
            logger.info("Login successful-> sending cookies :{}", response.getToken());

            //set token to null
            response.setToken(null);
            ResponseWrapper<LoginResponse> body = new ResponseWrapper<>(true, "Login successful", response);
            return ResponseEntity.ok(body);
        }catch (Exception e){
            logger.error(e.getMessage());
            ResponseWrapper<LoginResponse> body = new ResponseWrapper<>(false, e.getMessage(), null);
            return ResponseEntity.badRequest().body(body);

        }

    }

    @PostMapping("/logout")
    public ResponseEntity<ResponseWrapper<String>> logout(HttpServletResponse response) {
        // Clear cookies by setting maxAge to 0
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie csrfCookie = ResponseCookie.from("csrf-token", "")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie sessionCookie = ResponseCookie.from("sessionId", "")
                .httpOnly(false)
                .secure(false)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        response.addHeader("Set-Cookie", jwtCookie.toString());
        response.addHeader("Set-Cookie", csrfCookie.toString());
        response.addHeader("Set-Cookie", sessionCookie.toString());

        return ResponseEntity.ok(new ResponseWrapper<>(true, "Logout successful", null));
    }

    // in AuthController
    @PostMapping("/login/plain")
    public ResponseEntity<ResponseWrapper<LoginResponse>> loginPlain(
            @Valid @RequestBody LoginRequest req,
            HttpServletResponse httpResponse) {
        try {
            if (req.getEmail() == null || req.getPassword() == null) {
                return ResponseEntity.badRequest()
                        .body(new ResponseWrapper<>(false, "email and password required", null));
            }

            // Reuse your real login (validates creds, builds JWT inside LoginResponse)
            LoginResponse response = authService.login(req.getEmail(), req.getPassword());

            // Set cookies exactly like your normal flow (jwt + csrf-token + sessionId)
            authService.setAuthCookies(httpResponse, response.getToken());

            // IMPORTANT: DO NOT null the token here — we need it visible in Postman
            // response.setToken(null);  // <-- leave commented out for Postman testing

            return ResponseEntity.ok(new ResponseWrapper<>(true, "Login successful", response));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new ResponseWrapper<>(false, e.getMessage(), null));
        }
    }

}