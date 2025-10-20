//package com.aluminate.aluminate_organization_backend.controller.fallback;
//
//import com.aluminate.aluminate_organization_backend.service.dockerService.DockerService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//@RestController
//@RequestMapping("${api.prefix}/public/fallback")
//public class FallbackController {
//
//    private final DockerService dockerService = new DockerService();
//
//    @GetMapping("/create-org/{orgId}")
//    public ResponseEntity<String> createOrg(@PathVariable String orgId) {
//        try {
//            dockerService.createOrgContainer(orgId);
//            return ResponseEntity.ok("✅ Container started for org: " + orgId);
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity.internalServerError()
//                    .body("❌ Error starting container: " + e.getMessage());
//        }
//    }
//}
