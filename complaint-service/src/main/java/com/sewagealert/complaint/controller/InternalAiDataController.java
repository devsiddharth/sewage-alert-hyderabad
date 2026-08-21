package com.sewagealert.complaint.controller;

import com.sewagealert.complaint.dto.ApiResponse;
import com.sewagealert.complaint.dto.ComplaintResponse;
import com.sewagealert.complaint.model.Complaint;
import com.sewagealert.complaint.model.ComplaintPriority;
import com.sewagealert.complaint.model.ComplaintStatus;
import com.sewagealert.complaint.repository.ComplaintRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * InternalAiDataController: Internal endpoints for the AI Service to retrieve
 * complaint data for insights and analytics. NOT routed through the API Gateway.
 */
@RestController
@RequestMapping("/api/v1/internal/ai")
@Tag(name = "Internal AI Data", description = "Internal endpoints for AI complaint data retrieval — not routed through the API Gateway")
public class InternalAiDataController {

    private final ComplaintRepository complaintRepository;

    public InternalAiDataController(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @GetMapping("/complaints")
    @Operation(summary = "Get all complaints for AI context")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getAllComplaints() {
        List<ComplaintResponse> complaints = complaintRepository.findAll().stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved", complaints));
    }

    @GetMapping("/complaints/status/{status}")
    @Operation(summary = "Get complaints by status")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaintsByStatus(
            @PathVariable ComplaintStatus status) {
        List<ComplaintResponse> complaints = complaintRepository.findByStatus(status).stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved", complaints));
    }

    @GetMapping("/complaints/insights")
    @Operation(summary = "Get aggregated complaint insights for AI analytics")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComplaintInsights() {
        List<Complaint> all = complaintRepository.findAll();

        Map<String, Object> insights = new LinkedHashMap<>();
        insights.put("total", all.size());
        insights.put("pending", all.stream().filter(c -> c.getStatus() == ComplaintStatus.PENDING).count());
        insights.put("inProgress", all.stream().filter(c -> c.getStatus() == ComplaintStatus.IN_PROGRESS).count());
        insights.put("resolved", all.stream().filter(c -> c.getStatus() == ComplaintStatus.RESOLVED).count());
        insights.put("rejected", all.stream().filter(c -> c.getStatus() == ComplaintStatus.REJECTED).count());

        // Priority distribution
        Map<String, Long> priorityDist = all.stream()
                .filter(c -> c.getPriority() != null)
                .collect(Collectors.groupingBy(c -> c.getPriority().name(), Collectors.counting()));
        insights.put("priorityDistribution", priorityDist);

        // Status distribution
        Map<String, Long> statusDist = all.stream()
                .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));
        insights.put("statusDistribution", statusDist);

        // Top locations (by title grouping as proxy for area)
        Map<String, Long> titleCounts = all.stream()
                .collect(Collectors.groupingBy(Complaint::getTitle, Collectors.counting()));
        List<Map<String, Object>> topLocations = titleCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .map(e -> {
                    Map<String, Object> loc = new LinkedHashMap<>();
                    loc.put("title", e.getKey());
                    loc.put("count", e.getValue());
                    return loc;
                })
                .collect(Collectors.toList());
        insights.put("topLocations", topLocations);

        // Recurring locations (same title appearing more than once)
        List<Map<String, Object>> recurring = titleCounts.entrySet().stream()
                .filter(e -> e.getValue() > 1)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .map(e -> {
                    Map<String, Object> loc = new LinkedHashMap<>();
                    loc.put("title", e.getKey());
                    loc.put("count", e.getValue());
                    return loc;
                })
                .collect(Collectors.toList());
        insights.put("recurringLocations", recurring);

        // Overdue complaints (PENDING or IN_PROGRESS for more than 7 days)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
        long overdue = all.stream()
                .filter(c -> (c.getStatus() == ComplaintStatus.PENDING || c.getStatus() == ComplaintStatus.IN_PROGRESS)
                        && c.getCreatedAt() != null && c.getCreatedAt().isBefore(sevenDaysAgo))
                .count();
        insights.put("overdueCount", overdue);

        return ResponseEntity.ok(ApiResponse.success("Insights retrieved", insights));
    }

    @GetMapping("/complaints/user/{userId}")
    @Operation(summary = "Get complaints created by a specific user")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaintsByUser(@PathVariable Long userId) {
        List<ComplaintResponse> complaints = complaintRepository.findByCreatedBy(userId).stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("User complaints retrieved", complaints));
    }

    @GetMapping("/complaints/ngo/{ngoOrgId}")
    @Operation(summary = "Get complaints for AI NGO analytics (all complaints, filtered by NGO interest)")
    public ResponseEntity<ApiResponse<List<ComplaintResponse>>> getComplaintsForNgo(@PathVariable Long ngoOrgId) {
        // For NGO analytics, return all complaints since NGOs may address any complaint
        List<ComplaintResponse> complaints = complaintRepository.findAll().stream()
                .map(ComplaintResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Complaints retrieved", complaints));
    }

    @GetMapping("/complaints/correlation")
    @Operation(summary = "Get complaint-to-area correlation data for AI infrastructure insights")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getComplaintCorrelation() {
        List<Complaint> all = complaintRepository.findAll();

        Map<String, Object> correlation = new LinkedHashMap<>();

        // Group complaints by title (which contains the area/location info)
        Map<String, List<Complaint>> byTitle = all.stream()
                .collect(Collectors.groupingBy(Complaint::getTitle));

        // Build area-level summary
        List<Map<String, Object>> areaSummaries = byTitle.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> area = new LinkedHashMap<>();
                    area.put("area", entry.getKey());
                    area.put("totalComplaints", entry.getValue().size());

                    // Status breakdown per area
                    Map<String, Long> statusBreakdown = entry.getValue().stream()
                            .collect(Collectors.groupingBy(c -> c.getStatus().name(), Collectors.counting()));
                    area.put("statusBreakdown", statusBreakdown);

                    // Priority breakdown per area
                    Map<String, Long> priorityBreakdown = entry.getValue().stream()
                            .filter(c -> c.getPriority() != null)
                            .collect(Collectors.groupingBy(c -> c.getPriority().name(), Collectors.counting()));
                    area.put("priorityBreakdown", priorityBreakdown);

                    // Average coordinates for the area
                    OptionalDouble avgLat = entry.getValue().stream()
                            .mapToDouble(Complaint::getLatitude).average();
                    OptionalDouble avgLng = entry.getValue().stream()
                            .mapToDouble(Complaint::getLongitude).average();
                    if (avgLat.isPresent()) area.put("avgLatitude", avgLat.getAsDouble());
                    if (avgLng.isPresent()) area.put("avgLongitude", avgLng.getAsDouble());

                    // Pending count for the area
                    long pendingCount = entry.getValue().stream()
                            .filter(c -> c.getStatus() == ComplaintStatus.PENDING)
                            .count();
                    area.put("pendingCount", pendingCount);

                    // Overdue count (pending/in-progress for >7 days)
                    LocalDateTime sevenDaysAgo = LocalDateTime.now().minus(7, ChronoUnit.DAYS);
                    long overdueCount = entry.getValue().stream()
                            .filter(c -> (c.getStatus() == ComplaintStatus.PENDING || c.getStatus() == ComplaintStatus.IN_PROGRESS)
                                    && c.getCreatedAt() != null && c.getCreatedAt().isBefore(sevenDaysAgo))
                            .count();
                    area.put("overdueCount", overdueCount);

                    // Most recent complaint date
                    entry.getValue().stream()
                            .map(Complaint::getCreatedAt)
                            .filter(Objects::nonNull)
                            .max(LocalDateTime::compareTo)
                            .ifPresent(latest -> area.put("latestComplaint", latest.toString()));

                    return area;
                })
                .sorted((a, b) -> Long.compare(
                        (Long) b.get("totalComplaints"),
                        (Long) a.get("totalComplaints")))
                .collect(Collectors.toList());

        correlation.put("areaSummaries", areaSummaries);
        correlation.put("totalAreas", areaSummaries.size());
        correlation.put("totalComplaints", all.size());

        // High-priority areas (areas with CRITICAL or HIGH priority complaints)
        List<String> highPriorityAreas = all.stream()
                .filter(c -> c.getPriority() == ComplaintPriority.HIGH || c.getPriority() == ComplaintPriority.CRITICAL)
                .map(Complaint::getTitle)
                .distinct()
                .collect(Collectors.toList());
        correlation.put("highPriorityAreas", highPriorityAreas);

        return ResponseEntity.ok(ApiResponse.success("Correlation data retrieved", correlation));
    }
}
