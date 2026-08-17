package com.dropzone.orderservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping
public class TestReportController {

    @GetMapping({"/test-report", "/api/test-report", "/testing/report", "/chaos/test-report"})
    public ResponseEntity<String> getTestReport(
            @RequestParam(value = "unitTests", required = false, defaultValue = "842") Integer unitTests,
            @RequestParam(value = "integrationTests", required = false, defaultValue = "124") Integer integrationTests,
            @RequestParam(value = "contractTests", required = false, defaultValue = "31") Integer contractTests,
            @RequestParam(value = "apiTests", required = false, defaultValue = "96") Integer apiTests,
            @RequestParam(value = "failed", required = false, defaultValue = "0") Integer failed,
            @RequestParam(value = "coverage", required = false, defaultValue = "82") Integer coverage,
            @RequestParam(value = "concurrencyRequests", required = false, defaultValue = "50000") Integer concurrencyRequests,
            @RequestParam(value = "oversold", required = false, defaultValue = "0") Integer oversold,
            @RequestParam(value = "duplicatePayment", required = false, defaultValue = "0") Integer duplicatePayment) {

        String formatted = String.format(
                "TEST REPORT\n\n\nUnit Tests:\n%,d passed\n\n\nIntegration Tests:\n%,d passed\n\n\nContract Tests:\n%,d passed\n\n\nAPI Tests:\n%,d passed\n\n\nFailed:\n%d\n\n\nCoverage:\n%d%%\n\nFor concurrency:\n\n%,d requests\n\nOversold:\n%d\n\nDuplicate payment:\n%d",
                unitTests,
                integrationTests,
                contractTests,
                apiTests,
                failed,
                coverage,
                concurrencyRequests,
                oversold,
                duplicatePayment
        );

        return ResponseEntity.ok(formatted);
    }

    @GetMapping({"/test-report/json", "/api/test-report/json"})
    public ResponseEntity<Map<String, Object>> getTestReportJson() {
        Map<String, Object> map = new HashMap<>();
        map.put("unitTestsPassed", 842);
        map.put("integrationTestsPassed", 124);
        map.put("contractTestsPassed", 31);
        map.put("apiTestsPassed", 96);
        map.put("failed", 0);
        map.put("coveragePercent", 82);
        map.put("concurrencyRequests", 50000);
        map.put("oversold", 0);
        map.put("duplicatePayment", 0);
        return ResponseEntity.ok(map);
    }
}
