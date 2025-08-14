package com.aluminate.aluminate_organization_backend.controller.info;


import com.aluminate.aluminate_organization_backend.config.ResponseWrapper;
import com.aluminate.aluminate_organization_backend.dto.MemberDTO;
import com.aluminate.aluminate_organization_backend.dto.info.InfoUserDetailsResponse;
import com.aluminate.aluminate_organization_backend.service.info.infoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/member/info")
public class InfoControllerMember {
    private final infoService infoService;

    public InfoControllerMember(infoService infoService) {
        this.infoService = infoService;
    }

    // This controller will handle member-related information endpoints.
    // Currently, it does not have any methods defined.
    // You can add methods for member info retrieval, etc. as needed.
    @GetMapping("/getMemberInfo")
    public ResponseEntity<ResponseWrapper<InfoUserDetailsResponse>> getMemberInfo() {
        InfoUserDetailsResponse infoUserDetailsResponse = infoService.getUser();
        ResponseWrapper<InfoUserDetailsResponse> responseWrapper = new ResponseWrapper<>(
                true,
                "Member info retrieval successful",
                infoUserDetailsResponse
        );
        return ResponseEntity.ok(responseWrapper);

    }




}
