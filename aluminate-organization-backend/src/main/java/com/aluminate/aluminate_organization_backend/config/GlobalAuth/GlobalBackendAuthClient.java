package com.aluminate.aluminate_organization_backend.config.GlobalAuth;

import com.aluminate.aluminate_organization_backend.dto.login.GlobalAuthRequest;
import com.aluminate.aluminate_organization_backend.dto.login.GlobalAuthResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;





@FeignClient(name = "global-backend", url = "${global.backend.base-url}${api.prefix.global}")
public interface GlobalBackendAuthClient {

    @PostMapping("/auth/verify-admin")
    ResponseEntity<GlobalAuthResponse> verifyAdminCredentials(
            @RequestBody String encryptedRequest);

    @PostMapping("/public/payment/syncOrgTransactionTickets")
    ResponseEntity<Boolean> syncOrgTransactionTickets(
            @RequestBody String encryptedRequest
    );
}
