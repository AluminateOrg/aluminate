package com.aluminate.aluminate_organization_backend.controller.info;

import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.dto.info.InfoUserDetailsResponse;
import com.aluminate.aluminate_organization_backend.service.info.infoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/admin/info")
public class InfoControllerAdmin {
    private final infoService infoService;
    private final Logger logger = LoggerFactory.getLogger(InfoControllerAdmin.class);

    public InfoControllerAdmin(infoService infoService) {
        this.infoService = infoService;
    }

    @GetMapping("/getAdminInfo")
    public ResponseEntity<ResponseWrapper<InfoUserDetailsResponse>> getAdminInfo() {
        logger.info("getting AdminInfo...");
        InfoUserDetailsResponse infoUserDetailsResponse = infoService.getUser();
        logger.info("getAdminInfo successful");
        ResponseWrapper<InfoUserDetailsResponse> responseWrapper = new ResponseWrapper<>(
                true,
                "Admin info retrieval successful",
                infoUserDetailsResponse
        );
        return ResponseEntity.ok(responseWrapper);
    }
}
