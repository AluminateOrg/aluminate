package com.aluminate.aluminate_organization_backend.integrationTests;


import com.aluminate.aluminate_organization_backend.config.util.RSAEncryptionUtil;
import com.aluminate.aluminate_organization_backend.dto.login.GlobalAuthRequest;
import com.aluminate.aluminate_organization_backend.dto.login.GlobalAuthResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;

import java.security.PrivateKey;
import java.security.PublicKey;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class VerifyAdminTest {

    @Value("${encryption.organization.private-key}")
    private String orgPrivateKeyENV;

    @Value("${encryption.global.public-key}")
    private String globalPublicKeyENV;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private PrivateKey orgPrivateKey;
    private PublicKey globalPublicKey;

    @PostConstruct
    public void initKeys() throws Exception {
        this.orgPrivateKey = RSAEncryptionUtil.privateKeyFromPem(orgPrivateKeyENV);
        this.globalPublicKey = RSAEncryptionUtil.publicKeyFromPem(globalPublicKeyENV);
    }

    @Test
    void testVerifyAdminCredentials() throws Exception {
        // 1. Prepare the request
        GlobalAuthRequest request = new GlobalAuthRequest("senira277@gmail.com", "Abcd2002");
        String requestJson = objectMapper.writeValueAsString(request);

        // 2. Encrypt the request
        String encryptedRequest = RSAEncryptionUtil.encrypt(requestJson, globalPublicKey);

        // 3. Send to your running test server
        String url = "http://localhost:8080/api/v1/global/auth/verify-admin";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        HttpEntity<String> entity = new HttpEntity<>(encryptedRequest, headers);

        ResponseEntity<GlobalAuthResponse> response = restTemplate.postForEntity(url, entity, GlobalAuthResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());



        // 5. Validate
        assertTrue(response.getBody().isSuccess());
        logger.info("Admin Name: {}",response.getBody().getAdmin().getName());
    }
}

