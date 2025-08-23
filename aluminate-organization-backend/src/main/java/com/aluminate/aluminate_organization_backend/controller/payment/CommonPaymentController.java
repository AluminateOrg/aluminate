package com.aluminate.aluminate_organization_backend.controller.payment;

import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.dto.hash.HashRequest;
import com.aluminate.aluminate_organization_backend.dto.hash.HashResponse;
import com.aluminate.aluminate_organization_backend.model.Member;
import com.aluminate.aluminate_organization_backend.model.Organization;
import com.aluminate.aluminate_organization_backend.service.hash.HashService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/common/payment")
public class CommonPaymentController {
    private final HashService hashService;
    private final Logger logger = LoggerFactory.getLogger(CommonPaymentController.class);


    @Autowired
    public CommonPaymentController(HashService hashService) {
        logger.info("Initializing PaymentController");
        this.hashService = hashService;
    }

    @PostMapping("/generate-hash")
    public ResponseEntity<ResponseWrapper<HashResponse>> generateHash(@Valid @RequestBody HashRequest request) {
        logger.info("Generating hash for payment request: {}", request);
        //get the current admin and organization from the security context
        Member member = (Member) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Organization organization = member.getOrganization();
        HashResponse hashResponse = hashService.generateHash(request.getMode(),request.getAmount(), request.getCurrency(), organization, member);

        ResponseWrapper<HashResponse> body = new ResponseWrapper<>(
                true,
                "Hash generation successful",
                hashResponse
        );
        return ResponseEntity.ok(body);

    }


}
