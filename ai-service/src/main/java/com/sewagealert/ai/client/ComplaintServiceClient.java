package com.sewagealert.ai.client;

import com.sewagealert.ai.dto.ApiResponse;
import com.sewagealert.ai.dto.ComplaintData;
import com.sewagealert.ai.dto.CorrelationData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.Map;

/**
 * ComplaintServiceClient: Declarative OpenFeign client for COMPLAINT-SERVICE's internal AI data endpoints.
 * Uses Eureka service discovery (lb://COMPLAINT-SERVICE).
 */
@FeignClient(name = "COMPLAINT-SERVICE")
public interface ComplaintServiceClient {

    @GetMapping("/api/v1/internal/ai/complaints")
    ApiResponse<List<ComplaintData>> getAllComplaints();

    @GetMapping("/api/v1/internal/ai/complaints/insights")
    ApiResponse<Map<String, Object>> getComplaintInsights();

    @GetMapping("/api/v1/internal/ai/complaints/user/{userId}")
    ApiResponse<List<ComplaintData>> getComplaintsByUser(@PathVariable("userId") Long userId);

    @GetMapping("/api/v1/internal/ai/complaints/ngo/{ngoOrgId}")
    ApiResponse<List<ComplaintData>> getComplaintsForNgo(@PathVariable("ngoOrgId") Long ngoOrgId);

    @GetMapping("/api/v1/internal/ai/complaints/correlation")
    ApiResponse<CorrelationData> getComplaintCorrelation();
}
