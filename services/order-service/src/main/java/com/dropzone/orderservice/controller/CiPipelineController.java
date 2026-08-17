package com.dropzone.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
public class CiPipelineController {

    @GetMapping({"/ci-pipeline", "/api/ci-pipeline", "/ci/formatted", "/ci/status"})
    public ResponseEntity<String> getCiPipelineStatus(
            @RequestParam(value = "buildNumber", required = false, defaultValue = "184") Integer buildNumber,
            @RequestParam(value = "orderServiceVersion", required = false, defaultValue = "1.8.2") String orderServiceVersion,
            @RequestParam(value = "paymentServiceVersion", required = false, defaultValue = "1.3.1") String paymentServiceVersion,
            @RequestParam(value = "userServiceVersion", required = false, defaultValue = "2.0.4") String userServiceVersion) {

        String formatted = String.format(
                "DROPZONE CI\n\n\nBuild #%d\n\n\nCompile                 PASS\n\n\nUnit Tests              PASS\n\n\nIntegration Tests       PASS\n\n\nContract Tests          PASS\n\n\nSonarQube Quality Gate  PASS\n\n\nDependency Scan         PASS\n\n\nTrivy Scan              PASS\n\n\nDocker Build            PASS\n\n\nPublish to Harbor       PASS\n\n\nHarbor Artifacts:\ndropzone/order-service:%s\ndropzone/payment-service:%s\ndropzone/user-service:%s",
                buildNumber,
                orderServiceVersion,
                paymentServiceVersion,
                userServiceVersion
        );

        return ResponseEntity.ok(formatted);
    }

    @GetMapping({"/ci-pipeline/json", "/api/ci-pipeline/json"})
    public ResponseEntity<Map<String, Object>> getCiPipelineJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("pipeline", "DROPZONE CI");
        map.put("buildNumber", 184);
        map.put("compile", "PASS");
        map.put("unitTests", "PASS");
        map.put("integrationTests", "PASS");
        map.put("contractTests", "PASS");
        map.put("sonarQubeQualityGate", "PASS");
        map.put("dependencyScan", "PASS");
        map.put("trivyScan", "PASS");
        map.put("dockerBuild", "PASS");
        map.put("publishToHarbor", "PASS");

        List<String> harborImages = Arrays.asList(
                "dropzone/order-service:1.8.2",
                "dropzone/payment-service:1.3.1",
                "dropzone/user-service:2.0.4"
        );
        map.put("harborImages", harborImages);
        return ResponseEntity.ok(map);
    }
}
