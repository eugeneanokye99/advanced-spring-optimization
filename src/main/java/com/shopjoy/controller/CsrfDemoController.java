package com.shopjoy.controller;

import com.shopjoy.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Demo controller to demonstrate CSRF protection for form-based endpoints.
 * 
 * This controller is protected by CSRF tokens, unlike the JWT-based API endpoints.
 * CSRF protection is necessary for session-based authentication where browsers
 * automatically attach cookies to requests.
 */
@Slf4j
@Tag(name = "CSRF Demo", description = "Demonstration endpoints for CSRF protection with form-based authentication")
@RestController
@RequestMapping("/demo")
public class CsrfDemoController {

    /**
     * Endpoint to retrieve CSRF token for form submissions.
     * 
     * This endpoint returns the CSRF token that must be included in subsequent
     * form submissions to protected endpoints.
     * 
     * @param token the CSRF token automatically injected by Spring Security
     * @return the CSRF token details
     */
    @Operation(
            summary = "Get CSRF Token",
            description = "Retrieves the CSRF token required for form submissions. " +
                    "This token must be included in the request header (X-CSRF-TOKEN) or as a form parameter (_csrf) " +
                    "when submitting forms to CSRF-protected endpoints."
    )
    @GetMapping("/csrf-token")
    public ResponseEntity<Map<String, String>> getCsrfToken(CsrfToken token) {
        Map<String, String> response = new HashMap<>();
        response.put("token", token.getToken());
        response.put("headerName", token.getHeaderName());
        response.put("parameterName", token.getParameterName());
        response.put("message", "Include this token in your form submissions");
        
        log.debug("CSRF token requested - Header: {}, Parameter: {}", 
                token.getHeaderName(), token.getParameterName());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Demo form submission endpoint that requires CSRF token.
     * 
     * This endpoint demonstrates CSRF protection for form-based endpoints.
     * Requests without a valid CSRF token will be rejected with 403 Forbidden.
     * 
     * @param formData the form data submitted
     * @return success response if CSRF token is valid
     */
    @Operation(
            summary = "Submit Demo Form (CSRF Protected)",
            description = "Demonstrates CSRF protection for form submissions. " +
                    "This endpoint requires a valid CSRF token obtained from /demo/csrf-token. " +
                    "Include the token in the X-CSRF-TOKEN header or as a _csrf form parameter."
    )
    @PostMapping("/form-submit")
    public ResponseEntity<ApiResponse<FormSubmissionResponse>> submitForm(
            @RequestBody FormSubmissionRequest formData) {
        
        log.info("Form submitted successfully with CSRF protection - Name: {}, Message: {}", 
                formData.getName(), formData.getMessage());
        
        FormSubmissionResponse response = new FormSubmissionResponse(
                "Form submitted successfully!",
                formData.getName(),
                formData.getMessage(),
                LocalDateTime.now(),
                "CSRF token was validated successfully"
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "Form submission successful"));
    }

    /**
     * Demo endpoint showing protected DELETE operation.
     * 
     * DELETE operations are particularly vulnerable to CSRF attacks.
     * This endpoint demonstrates CSRF protection for dangerous operations.
     */
    @Operation(
            summary = "Delete Demo Resource (CSRF Protected)",
            description = "Demonstrates CSRF protection for dangerous operations like DELETE. " +
                    "Requires valid CSRF token to prevent unauthorized deletions."
    )
    @DeleteMapping("/resource/{id}")
    public ResponseEntity<ApiResponse<String>> deleteResource(@PathVariable Long id) {
        log.info("Resource {} deleted successfully with CSRF protection", id);
        
        return ResponseEntity.ok(ApiResponse.success(
                "Resource " + id + " deleted",
                "Deletion successful - CSRF token validated"
        ));
    }

    /**
     * Safe GET endpoint - does not require CSRF protection.
     * 
     * GET requests should not modify state, so CSRF protection is not needed.
     * This endpoint is accessible without CSRF token.
     */
    @Operation(
            summary = "Get Demo Data (No CSRF Required)",
            description = "Safe GET endpoint that does not require CSRF token. " +
                    "GET requests should be read-only and idempotent, making them safe from CSRF attacks."
    )
    @GetMapping("/data")
    public ResponseEntity<ApiResponse<Map<String, String>>> getData() {
        Map<String, String> data = new HashMap<>();
        data.put("message", "This is safe GET endpoint");
        data.put("csrfRequired", "false");
        data.put("reason", "GET requests should not modify state");
        
        return ResponseEntity.ok(ApiResponse.success(data, "Data retrieved successfully"));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormSubmissionRequest {
        private String name;
        private String message;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormSubmissionResponse {
        private String status;
        private String name;
        private String message;
        private LocalDateTime timestamp;
        private String csrfStatus;
    }
}
